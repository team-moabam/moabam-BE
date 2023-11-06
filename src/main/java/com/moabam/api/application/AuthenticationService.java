package com.moabam.api.application;

import static com.moabam.global.common.util.OAuthParameterNames.*;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import com.moabam.api.dto.AuthorizationCodeRequest;
import com.moabam.api.dto.AuthorizationCodeResponse;
import com.moabam.api.dto.AuthorizationTokenInfoResponse;
import com.moabam.api.dto.AuthorizationTokenRequest;
import com.moabam.api.dto.AuthorizationTokenResponse;
import com.moabam.api.dto.LoginResponse;
import com.moabam.api.dto.OAuthMapper;
import com.moabam.global.common.util.CookieUtils;
import com.moabam.global.common.util.GlobalConstant;
import com.moabam.global.common.util.TokenConstant;
import com.moabam.global.config.OAuthConfig;
import com.moabam.global.error.exception.BadRequestException;
import com.moabam.global.error.model.ErrorMessage;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

	private final OAuthConfig oAuthConfig;
	private final OAuth2AuthorizationServerRequestService oauth2AuthorizationServerRequestService;
	private final MemberService memberService;
	private final JwtProviderService jwtProviderService;

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
		issueServiceToken(httpServletResponse, loginResponse.id());

		return loginResponse;
	}

	private String getAuthorizationCodeUri() {
		AuthorizationCodeRequest authorizationCodeRequest = OAuthMapper.toAuthorizationCodeRequest(oAuthConfig);
		return generateQueryParamsWith(authorizationCodeRequest);
	}

	private String generateTokenValue(String token) {
		return TokenConstant.TOKEN_TYPE + GlobalConstant.SPACE + token;
	}

	private String generateQueryParamsWith(AuthorizationCodeRequest authorizationCodeRequest) {
		UriComponentsBuilder authorizationCodeUri = UriComponentsBuilder.fromUriString(
				oAuthConfig.provider().authorizationUri())
			.queryParam(RESPONSE_TYPE, CODE)
			.queryParam(CLIENT_ID, authorizationCodeRequest.clientId())
			.queryParam(REDIRECT_URI, authorizationCodeRequest.redirectUri());

		if (!authorizationCodeRequest.scope().isEmpty()) {
			String scopes = String.join(GlobalConstant.COMMA, authorizationCodeRequest.scope());
			authorizationCodeUri.queryParam(SCOPE, scopes);
		}

		return authorizationCodeUri.toUriString();
	}

	private void validAuthorizationGrant(String code) {
		if (code == null) {
			throw new BadRequestException(ErrorMessage.GRANT_FAILED);
		}
	}

	private AuthorizationTokenResponse issueTokenToAuthorizationServer(String code) {
		AuthorizationTokenRequest authorizationTokenRequest = OAuthMapper.toAuthorizationTokenRequest(oAuthConfig,
			code);
		MultiValueMap<String, String> uriParams = generateTokenRequest(authorizationTokenRequest);
		ResponseEntity<AuthorizationTokenResponse> authorizationTokenResponse =
			oauth2AuthorizationServerRequestService.requestAuthorizationServer(oAuthConfig.provider().tokenUri(),
				uriParams);

		return authorizationTokenResponse.getBody();
	}

	private MultiValueMap<String, String> generateTokenRequest(AuthorizationTokenRequest authorizationTokenRequest) {
		MultiValueMap<String, String> contents = new LinkedMultiValueMap<>();
		contents.add(GRANT_TYPE, authorizationTokenRequest.grantType());
		contents.add(CLIENT_ID, authorizationTokenRequest.clientId());
		contents.add(REDIRECT_URI, authorizationTokenRequest.redirectUri());
		contents.add(CODE, authorizationTokenRequest.code());

		if (authorizationTokenRequest.clientSecret() != null) {
			contents.add(CLIENT_SECRET, authorizationTokenRequest.clientSecret());
		}

		return contents;
	}

	private void issueServiceToken(HttpServletResponse response, Long id) {
		response.addHeader("token_type", "Bearer");
		response.addCookie(CookieUtils.tokenCookie("access_token", jwtProviderService.provideAccessToken(id)));
		response.addCookie(CookieUtils.tokenCookie("refresh_token", jwtProviderService.provideRefreshToken(id)));
	}
}
