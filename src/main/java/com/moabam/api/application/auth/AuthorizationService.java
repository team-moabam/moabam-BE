package com.moabam.api.application.auth;

import java.util.Arrays;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import com.moabam.admin.application.admin.AdminService;
import com.moabam.api.application.auth.mapper.AuthMapper;
import com.moabam.api.application.auth.mapper.AuthorizationMapper;
import com.moabam.api.application.member.MemberService;
import com.moabam.api.domain.auth.repository.TokenRepository;
import com.moabam.api.domain.member.Member;
import com.moabam.api.domain.member.Role;
import com.moabam.api.dto.auth.AuthorizationCodeRequest;
import com.moabam.api.dto.auth.AuthorizationCodeResponse;
import com.moabam.api.dto.auth.AuthorizationTokenInfoResponse;
import com.moabam.api.dto.auth.AuthorizationTokenRequest;
import com.moabam.api.dto.auth.AuthorizationTokenResponse;
import com.moabam.api.dto.auth.LoginResponse;
import com.moabam.api.dto.auth.TokenSaveValue;
import com.moabam.api.infrastructure.fcm.FcmService;
import com.moabam.global.auth.model.AuthMember;
import com.moabam.global.auth.model.PublicClaim;
import com.moabam.global.common.util.CookieUtils;
import com.moabam.global.common.util.GlobalConstant;
import com.moabam.global.config.AllowOriginConfig;
import com.moabam.global.config.OAuthConfig;
import com.moabam.global.config.TokenConfig;
import com.moabam.global.error.exception.BadRequestException;
import com.moabam.global.error.exception.UnauthorizedException;
import com.moabam.global.error.model.ErrorMessage;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthorizationService {

	private final FcmService fcmService;
	private final OAuthConfig oAuthConfig;
	private final TokenConfig tokenConfig;
	private final OAuth2AuthorizationServerRequestService oauth2AuthorizationServerRequestService;
	private final MemberService memberService;
	private final AdminService adminService;
	private final JwtProviderService jwtProviderService;
	private final TokenRepository tokenRepository;
	private final AllowOriginConfig allowOriginsConfig;

	public void redirectToLoginPage(HttpServletResponse httpServletResponse) {
		String authorizationCodeUri = getAuthorizationCodeUri();
		oauth2AuthorizationServerRequestService.loginRequest(httpServletResponse, authorizationCodeUri);
	}

	public AuthorizationTokenResponse requestAdminToken(AuthorizationCodeResponse authorizationCodeResponse) {
		validAuthorizationGrant(authorizationCodeResponse.code());

		return issueTokenToAuthorizationServer(authorizationCodeResponse.code(),
			oAuthConfig.provider().adminRedirectUri());
	}

	public AuthorizationTokenResponse requestToken(AuthorizationCodeResponse authorizationCodeResponse) {
		validAuthorizationGrant(authorizationCodeResponse.code());

		return issueTokenToAuthorizationServer(authorizationCodeResponse.code(), oAuthConfig.provider().redirectUri());
	}

	public AuthorizationTokenInfoResponse requestTokenInfo(AuthorizationTokenResponse authorizationTokenResponse) {
		String tokenValue = generateTokenValue(authorizationTokenResponse.accessToken());
		ResponseEntity<AuthorizationTokenInfoResponse> authorizationTokenInfoResponse =
			oauth2AuthorizationServerRequestService.tokenInfoRequest(oAuthConfig.provider().tokenInfo(), tokenValue);

		return authorizationTokenInfoResponse.getBody();
	}

	public LoginResponse signUpOrLogin(HttpServletResponse httpServletResponse,
		AuthorizationTokenInfoResponse authorizationTokenInfoResponse) {
		LoginResponse loginResponse = memberService.login(authorizationTokenInfoResponse);
		issueServiceToken(httpServletResponse, loginResponse.publicClaim());

		return loginResponse;
	}

	public void issueServiceToken(HttpServletResponse response, PublicClaim publicClaim) {
		String accessToken = jwtProviderService.provideAccessToken(publicClaim);
		String refreshToken = jwtProviderService.provideRefreshToken(publicClaim.role());
		TokenSaveValue tokenSaveRequest = AuthMapper.toTokenSaveValue(refreshToken, null);

		tokenRepository.saveToken(publicClaim.id(), tokenSaveRequest, publicClaim.role());

		String domain = getDomain(publicClaim.role());

		response.addCookie(CookieUtils.typeCookie("Bearer", tokenConfig.getRefreshExpire(), domain));
		response.addCookie(
			CookieUtils.tokenCookie("access_token", accessToken, tokenConfig.getRefreshExpire(), domain));
		response.addCookie(
			CookieUtils.tokenCookie("refresh_token", refreshToken, tokenConfig.getRefreshExpire(), domain));
	}

	public void validTokenPair(Long id, String oldRefreshToken, Role role) {
		TokenSaveValue tokenSaveValue = tokenRepository.getTokenSaveValue(id, role);

		if (!tokenSaveValue.refreshToken().equals(oldRefreshToken)) {
			tokenRepository.delete(id, role);

			throw new UnauthorizedException(ErrorMessage.AUTHENTICATE_FAIL);
		}
	}

	public void logout(AuthMember authMember, HttpServletRequest httpServletRequest,
		HttpServletResponse httpServletResponse) {
		removeToken(httpServletRequest, httpServletResponse);
		tokenRepository.delete(authMember.id(), authMember.role());
		fcmService.deleteTokenByMemberId(authMember.id());
	}

	public void removeToken(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
		if (httpServletRequest.getCookies() == null) {
			return;
		}

		Arrays.stream(httpServletRequest.getCookies()).forEach(cookie -> {
			if (cookie.getName().contains("token")) {
				httpServletResponse.addCookie(CookieUtils.deleteCookie(cookie));
			}
		});
	}

	@Transactional
	public void unLinkMember(AuthMember authMember) {
		Member member = memberService.findMemberToDelete(authMember.id());
		unlinkRequest(member.getSocialId());
		memberService.delete(member);
	}

	private String getDomain(Role role) {
		if (role.equals(Role.ADMIN)) {
			return allowOriginsConfig.adminDomain();
		}

		return allowOriginsConfig.domain();
	}

	private void unlinkRequest(String socialId) {
		try {
			oauth2AuthorizationServerRequestService.unlinkMemberRequest(oAuthConfig.provider().unlink(),
				oAuthConfig.client().adminKey(), unlinkRequestParam(socialId));
			log.info("회원 탈퇴 성공 : [socialId={}]", socialId);
		} catch (BadRequestException badRequestException) {
			log.warn("회원 탈퇴요청 실패 : 카카오 연결 오류");
			throw new BadRequestException(ErrorMessage.UNLINK_REQUEST_FAIL_ROLLBACK_SUCCESS);
		}
	}

	private MultiValueMap<String, String> unlinkRequestParam(String socialId) {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add("target_id_type", "user_id");
		params.add("target_id", socialId);

		return params;
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

		if (authorizationCodeRequest.scope() != null && !authorizationCodeRequest.scope().isEmpty()) {
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

	private AuthorizationTokenResponse issueTokenToAuthorizationServer(String code, String redirectUri) {
		AuthorizationTokenRequest authorizationTokenRequest = AuthorizationMapper.toAuthorizationTokenRequest(
			oAuthConfig, code, redirectUri);
		MultiValueMap<String, String> uriParams = generateTokenRequest(authorizationTokenRequest);
		ResponseEntity<AuthorizationTokenResponse> authorizationTokenResponse =
			oauth2AuthorizationServerRequestService
				.requestAuthorizationServer(oAuthConfig.provider().tokenUri(), uriParams);

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

	public void validMemberExist(Long id, Role role) {
		if (role.equals(Role.ADMIN)) {
			adminService.findMember(id);

			return;
		}

		memberService.findMember(id);
	}
}
