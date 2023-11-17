package com.moabam.api.application.auth;

import java.util.Arrays;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import com.moabam.api.application.auth.mapper.AuthMapper;
import com.moabam.api.application.auth.mapper.AuthorizationMapper;
import com.moabam.api.application.member.MemberService;
import com.moabam.api.dto.auth.AuthorizationCodeRequest;
import com.moabam.api.dto.auth.AuthorizationCodeResponse;
import com.moabam.api.dto.auth.AuthorizationTokenInfoResponse;
import com.moabam.api.dto.auth.AuthorizationTokenRequest;
import com.moabam.api.dto.auth.AuthorizationTokenResponse;
import com.moabam.api.dto.auth.LoginResponse;
import com.moabam.api.dto.auth.TokenSaveValue;
import com.moabam.api.infrastructure.redis.TokenRepository;
import com.moabam.global.auth.model.AuthorizationMember;
import com.moabam.global.auth.model.PublicClaim;
import com.moabam.global.common.util.GlobalConstant;
import com.moabam.global.common.util.cookie.CookieUtils;
import com.moabam.global.config.OAuthConfig;
import com.moabam.global.config.TokenConfig;
import com.moabam.global.error.exception.BadRequestException;
import com.moabam.global.error.exception.UnauthorizedException;
import com.moabam.global.error.model.ErrorMessage;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthorizationService {

	private final OAuthConfig oAuthConfig;
	private final TokenConfig tokenConfig;
	private final OAuth2AuthorizationServerRequestService oauth2AuthorizationServerRequestService;
	private final MemberService memberService;
	private final JwtProviderService jwtProviderService;
	private final TokenRepository tokenRepository;
	private final CookieUtils cookieUtils;

	public void redirectToLoginPage(HttpServletResponse httpServletResponse) {
		String authorizationCodeUri = getAuthorizationCodeUri();
		oauth2AuthorizationServerRequestService.loginRequest(httpServletResponse, authorizationCodeUri);
	}

	public AuthorizationTokenResponse requestToken(AuthorizationCodeResponse authorizationCodeResponse) {
		validAuthorizationGrant(authorizationCodeResponse.code());

		return issueTokenToAuthorizationServer(authorizationCodeResponse.code());
	}

	public AuthorizationTokenInfoResponse requestTokenInfo(AuthorizationTokenResponse authorizationTokenResponse) {
		String tokenValue = generateTokenValue(authorizationTokenResponse.accessToken());
		ResponseEntity<AuthorizationTokenInfoResponse> authorizationTokenInfoResponse =
			oauth2AuthorizationServerRequestService.tokenInfoRequest(oAuthConfig.provider().tokenInfo(), tokenValue);

		return authorizationTokenInfoResponse.getBody();
	}

	@Transactional
	public LoginResponse signUpOrLogin(HttpServletResponse httpServletResponse,
		AuthorizationTokenInfoResponse authorizationTokenInfoResponse) {
		LoginResponse loginResponse = memberService.login(authorizationTokenInfoResponse);
		issueServiceToken(httpServletResponse, loginResponse.publicClaim());

		return loginResponse;
	}

	public void issueServiceToken(HttpServletResponse response, PublicClaim publicClaim) {
		String accessToken = jwtProviderService.provideAccessToken(publicClaim);
		String refreshToken = jwtProviderService.provideRefreshToken();
		TokenSaveValue tokenSaveRequest = AuthMapper.toTokenSaveValue(refreshToken, null);

		tokenRepository.saveToken(publicClaim.id(), tokenSaveRequest);

		response.addCookie(
			cookieUtils.typeCookie("Bearer", tokenConfig.getRefreshExpire()));
		response.addCookie(
			cookieUtils.tokenCookie("access_token", accessToken, tokenConfig.getRefreshExpire()));
		response.addCookie(
			cookieUtils.tokenCookie("refresh_token", refreshToken, tokenConfig.getRefreshExpire()));
	}

	public void validTokenPair(Long id, String oldRefreshToken) {
		TokenSaveValue tokenSaveValue = tokenRepository.getTokenSaveValue(id);

		if (!tokenSaveValue.refreshToken().equals(oldRefreshToken)) {
			tokenRepository.delete(id);

			throw new UnauthorizedException(ErrorMessage.AUTHENTICATE_FAIL);
		}
	}

	public void logout(AuthorizationMember authorizationMember, HttpServletRequest httpServletRequest,
		HttpServletResponse httpServletResponse) {
		removeToken(httpServletRequest, httpServletResponse);
		tokenRepository.delete(authorizationMember.id());
	}

	public void removeToken(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
		if (httpServletRequest.getCookies() == null) {
			return;
		}

		Arrays.stream(httpServletRequest.getCookies())
			.forEach(cookie -> {
				if (cookie.getName().contains("token")) {
					httpServletResponse.addCookie(cookieUtils.deleteCookie(cookie));
				}
			});
	}

	private String getAuthorizationCodeUri() {
		AuthorizationCodeRequest authorizationCodeRequest = AuthorizationMapper.toAuthorizationCodeRequest(oAuthConfig);
		return generateQueryParamsWith(authorizationCodeRequest);
	}

	private String generateTokenValue(String token) {
		return "Bearer" + GlobalConstant.SPACE + token;
	}

	private String generateQueryParamsWith(AuthorizationCodeRequest authorizationCodeRequest) {
		UriComponentsBuilder authorizationCodeUri = UriComponentsBuilder.fromUriString(
				oAuthConfig.provider().authorizationUri())
			.queryParam("response_type", "code")
			.queryParam("client_id", authorizationCodeRequest.clientId())
			.queryParam("redirect_uri", authorizationCodeRequest.redirectUri());

		if (!authorizationCodeRequest.scope().isEmpty()) {
			String scopes = String.join(",", authorizationCodeRequest.scope());
			authorizationCodeUri.queryParam("scope", scopes);
		}

		return authorizationCodeUri.toUriString();
	}

	private void validAuthorizationGrant(String code) {
		if (code == null) {
			throw new BadRequestException(ErrorMessage.GRANT_FAILED);
		}
	}

	private AuthorizationTokenResponse issueTokenToAuthorizationServer(String code) {
		AuthorizationTokenRequest authorizationTokenRequest = AuthorizationMapper.toAuthorizationTokenRequest(
			oAuthConfig,
			code);
		MultiValueMap<String, String> uriParams = generateTokenRequest(authorizationTokenRequest);
		ResponseEntity<AuthorizationTokenResponse> authorizationTokenResponse =
			oauth2AuthorizationServerRequestService.requestAuthorizationServer(oAuthConfig.provider().tokenUri(),
				uriParams);

		return authorizationTokenResponse.getBody();
	}

	private MultiValueMap<String, String> generateTokenRequest(AuthorizationTokenRequest authorizationTokenRequest) {
		MultiValueMap<String, String> contents = new LinkedMultiValueMap<>();
		contents.add("grant_type", authorizationTokenRequest.grantType());
		contents.add("client_id", authorizationTokenRequest.clientId());
		contents.add("redirect_uri", authorizationTokenRequest.redirectUri());
		contents.add("code", authorizationTokenRequest.code());

		if (authorizationTokenRequest.clientSecret() != null) {
			contents.add("client_secret", authorizationTokenRequest.clientSecret());
		}

		return contents;
	}
}
