package com.moabam.api.application;

import static com.moabam.global.common.util.OAuthParameterNames.*;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import com.moabam.api.dto.AuthorizationCodeRequest;
import com.moabam.api.dto.AuthorizationCodeResponse;
import com.moabam.api.dto.AuthorizationTokenRequest;
import com.moabam.api.dto.AuthorizationTokenResponse;
import com.moabam.api.dto.OAuthMapper;
import com.moabam.global.common.util.GlobalConstant;
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

	private String getAuthorizationCodeUri() {
		AuthorizationCodeRequest authorizationCodeRequest = OAuthMapper.toAuthorizationCodeRequest(oAuthConfig);
		return generateQueryParamsWith(authorizationCodeRequest);
	}

	private String generateQueryParamsWith(AuthorizationCodeRequest authorizationCodeRequest) {
		UriComponentsBuilder authorizationCodeUri = UriComponentsBuilder
			.fromUriString(oAuthConfig.provider().authorizationUri())
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

	public void redirectToLoginPage(HttpServletResponse httpServletResponse) {
		String authorizationCodeUri = getAuthorizationCodeUri();
		oauth2AuthorizationServerRequestService.loginRequest(httpServletResponse, authorizationCodeUri);
	}

	public void requestToken(AuthorizationCodeResponse authorizationCodeResponse) {
		validAuthorizationGrant(authorizationCodeResponse.code());
		issueTokenToAuthorizationServer(authorizationCodeResponse.code());
		// TODO 발급한 토큰으로 사용자의 정보 얻어와야함 : 프로필 & 닉네임
	}
}
