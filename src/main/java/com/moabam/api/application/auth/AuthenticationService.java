package com.moabam.api.application.auth;

import com.moabam.api.dto.AuthorizationCodeRequest;
import com.moabam.global.config.OAuthConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

	private final OAuthConfig oAuthConfig;

	public AuthorizationCodeRequest authorizaionCodeParams() {
		return AuthorizationCodeRequest.builder()
			.clientId(oAuthConfig.client().clientId())
			.redirectUri(oAuthConfig.provider().redirectUrl())
			.scope(oAuthConfig.client().scope())
			.build();
	}

	public String getUrl() {
		return oAuthConfig.provider().authorizationUrl();
	}
}
