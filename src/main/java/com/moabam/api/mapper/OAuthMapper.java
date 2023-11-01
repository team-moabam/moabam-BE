package com.moabam.api.mapper;

import com.moabam.api.dto.AuthorizationCodeRequest;
import com.moabam.global.config.OAuthConfig;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OAuthMapper {

	public static AuthorizationCodeRequest toAuthorizationCodeRequest(OAuthConfig oAuthConfig) {
		return AuthorizationCodeRequest.builder()
			.clientId(oAuthConfig.client().clientId())
			.redirectUri(oAuthConfig.provider().redirectUri())
			.scope(oAuthConfig.client().scope())
			.build();
	}

}
