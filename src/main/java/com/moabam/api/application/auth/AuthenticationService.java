package com.moabam.api.application.auth;

import org.springframework.stereotype.Service;

import com.moabam.api.dto.auth.AuthorizationCodeIssue;
import com.moabam.global.config.OAuthConfig;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

	private final OAuthConfig oAuthConfig;

	public String getAuthorizaionCodeUri() {
		return AuthorizationCodeIssue.builder()
			.clientId(oAuthConfig.client().clientId())
			.redirectUri(oAuthConfig.provider().redirectUri())
			.scope(oAuthConfig.client().scope())
			.build()
			.generateQueryParamsWith(oAuthConfig.provider().authorizationUri());
	}
}
