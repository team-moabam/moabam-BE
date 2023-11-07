package com.moabam.api.dto;

import com.moabam.global.config.OAuthConfig;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OAuthMapper {

	public static AuthorizationCodeRequest toAuthorizationCodeRequest(OAuthConfig oAuthConfig) {
		return AuthorizationCodeRequest.builder()
			.clientId(oAuthConfig.client().clientId())
			.redirectUri(oAuthConfig.provider().redirectUri())
			.scope(oAuthConfig.client().scope())
			.build();
	}

	public static AuthorizationTokenRequest toAuthorizationTokenRequest(OAuthConfig oAuthConfig, String code) {
		return AuthorizationTokenRequest.builder()
			.grantType(oAuthConfig.client().authorizationGrantType())
			.clientId(oAuthConfig.client().clientId())
			.redirectUri(oAuthConfig.provider().redirectUri())
			.code(code)
			.clientSecret(oAuthConfig.client().clientSecret())
			.build();
	}
}
