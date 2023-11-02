package com.moabam.api.application;

import java.io.IOException;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.moabam.api.dto.AuthorizationTokenInfoResponse;
import com.moabam.api.dto.AuthorizationTokenResponse;
import com.moabam.global.common.util.GlobalConstant;
import com.moabam.global.common.util.TokenConstant;
import com.moabam.global.error.exception.BadRequestException;
import com.moabam.global.error.model.ErrorMessage;

import jakarta.servlet.http.HttpServletResponse;

@Service
public class OAuth2AuthorizationServerRequestService {

	private final RestTemplate restTemplate;

	public OAuth2AuthorizationServerRequestService() {
		restTemplate = new RestTemplate();
	}

	public void loginRequest(HttpServletResponse httpServletResponse, String authorizationCodeUri) {
		try {
			httpServletResponse.setContentType(MediaType.APPLICATION_FORM_URLENCODED + GlobalConstant.CHARSET_UTF_8);
			httpServletResponse.sendRedirect(authorizationCodeUri);
		} catch (IOException e) {
			throw new BadRequestException(ErrorMessage.REQUEST_FAILED);
		}
	}

	public ResponseEntity<AuthorizationTokenResponse> requestAuthorizationServer(String tokenUri,
		MultiValueMap<String, String> uriParams) {
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_TYPE,
			MediaType.APPLICATION_FORM_URLENCODED_VALUE + GlobalConstant.CHARSET_UTF_8);
		HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(uriParams, headers);

		ResponseEntity<AuthorizationTokenResponse> authorizationTokenResponse = restTemplate.exchange(tokenUri,
			HttpMethod.POST, httpEntity, AuthorizationTokenResponse.class);

		if (authorizationTokenResponse.getStatusCode().isError()) {
			throw new BadRequestException(ErrorMessage.REQUEST_FAILED);
		}

		return authorizationTokenResponse;
	}

	public ResponseEntity<AuthorizationTokenInfoResponse> tokenInfoRequest(String tokenInfoUri, String tokenValue) {
		HttpHeaders headers = new HttpHeaders();
		headers.add(TokenConstant.AUTHORIZATION, tokenValue);
		HttpEntity<Void> httpEntity = new HttpEntity<>(headers);

		ResponseEntity<AuthorizationTokenInfoResponse> authorizationTokenInfoResponseResponse =
			restTemplate.exchange(tokenInfoUri, HttpMethod.GET, httpEntity, AuthorizationTokenInfoResponse.class);

		if (authorizationTokenInfoResponseResponse.getStatusCode().isError()) {
			throw new BadRequestException(ErrorMessage.REQUEST_FAILED);
		}

		return authorizationTokenInfoResponseResponse;
	}
}
