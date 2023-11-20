package com.moabam.api.application.auth;

import java.io.IOException;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.moabam.api.dto.auth.AuthorizationTokenInfoResponse;
import com.moabam.api.dto.auth.AuthorizationTokenResponse;
import com.moabam.api.dto.auth.UnlinkMemberRequest;
import com.moabam.api.dto.auth.UnlinkMemberResponse;
import com.moabam.global.common.util.GlobalConstant;
import com.moabam.global.error.exception.BadRequestException;
import com.moabam.global.error.handler.RestTemplateResponseHandler;
import com.moabam.global.error.model.ErrorMessage;

import jakarta.servlet.http.HttpServletResponse;

@Service
public class OAuth2AuthorizationServerRequestService {

	private final RestTemplate restTemplate;

	public OAuth2AuthorizationServerRequestService() {
		restTemplate = new RestTemplateBuilder()
			.errorHandler(new RestTemplateResponseHandler())
			.build();
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

		return restTemplate.exchange(tokenUri, HttpMethod.POST, httpEntity, AuthorizationTokenResponse.class);
	}

	public ResponseEntity<AuthorizationTokenInfoResponse> tokenInfoRequest(String tokenInfoUri, String tokenValue) {
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", tokenValue);
		HttpEntity<Void> httpEntity = new HttpEntity<>(headers);

		return restTemplate.exchange(tokenInfoUri, HttpMethod.GET, httpEntity, AuthorizationTokenInfoResponse.class);
	}

	public void unlinkMemberRequest(String unlinkUri, String adminKey,
		String socialId) {
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_TYPE,
			MediaType.APPLICATION_FORM_URLENCODED_VALUE + GlobalConstant.CHARSET_UTF_8);
		headers.add("Authorization", "KakaoAK " + adminKey);
		HttpEntity<UnlinkMemberRequest> httpEntity = new HttpEntity<>(UnlinkMemberRequest.of(socialId), headers);

		restTemplate.exchange(unlinkUri, HttpMethod.POST, httpEntity, UnlinkMemberResponse.class);
	}
}
