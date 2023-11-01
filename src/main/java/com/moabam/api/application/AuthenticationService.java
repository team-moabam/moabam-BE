package com.moabam.api.application;

import static com.moabam.global.common.util.OAuthParameterNames.*;

import java.io.IOException;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
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

	private void validAuthorizationGrant(AuthorizationCodeResponse authorizationCodeResponse) {
		if (authorizationCodeResponse.code() == null) {
			throw new BadRequestException(ErrorMessage.GRANT_FAILED);
		}
	}

	private AuthorizationTokenResponse issueTokenToAuthorizationServer(String code) {
		AuthorizationTokenRequest authorizationTokenRequest = OAuthMapper.toAuthorizationTokenRequest(oAuthConfig,
			code);
		ResponseEntity<AuthorizationTokenResponse> authorizationTokenResponse = requestAuthorizationServer(
			authorizationTokenRequest);

		if (authorizationTokenResponse.getStatusCode().isError()) {
			throw new BadRequestException(ErrorMessage.REQUEST_FAILED);
		}

		return authorizationTokenResponse.getBody();
	}

	private ResponseEntity<AuthorizationTokenResponse> requestAuthorizationServer(
		AuthorizationTokenRequest authorizationTokenRequest) {
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_TYPE,
			MediaType.APPLICATION_FORM_URLENCODED_VALUE + GlobalConstant.CHARSET_UTF_8);
		HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(
			generateTokenRequest(authorizationTokenRequest), headers);

		return new RestTemplate().exchange(
			oAuthConfig.provider().tokenUri(), HttpMethod.POST,
			httpEntity, AuthorizationTokenResponse.class);
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

		try {
			httpServletResponse.setContentType(MediaType.APPLICATION_FORM_URLENCODED + GlobalConstant.CHARSET_UTF_8);
			httpServletResponse.sendRedirect(authorizationCodeUri);
		} catch (IOException e) {
			throw new BadRequestException(ErrorMessage.REQUEST_FAILED);
		}
	}

	public void requestToken(AuthorizationCodeResponse authorizationCodeResponse) {
		validAuthorizationGrant(authorizationCodeResponse);
		AuthorizationTokenResponse authorizationTokenResponse = issueTokenToAuthorizationServer(
			authorizationCodeResponse.code());
	}
}
