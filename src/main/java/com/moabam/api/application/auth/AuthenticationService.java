package com.moabam.api.application.auth;

import com.moabam.global.common.util.GlobalConstant;
import com.moabam.global.common.util.UrlGenerator;
import com.moabam.global.config.OAuthConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

	private static final String RESPONSE_TYPE = "response_type";
	private static final String CODE = "code";
	private static final String CLIENT_ID = "client_id";
	private static final String REDIRECT_URL = "redirect_url";
	private static final String SCOPE = "scope";

	private final OAuthConfig oAuthConfig;

	public String authorizaionCodeUrl() {
		String scopes = String.join(GlobalConstant.COMMA, oAuthConfig.client().scope());
		UrlGenerator url = UrlGenerator.builder()
			.baseUrl(oAuthConfig.provider().authorizationUrl())
			.parameter(RESPONSE_TYPE, CODE)
			.parameter(CLIENT_ID, oAuthConfig.client().clientId())
			.parameter(REDIRECT_URL, oAuthConfig.provider().redirectUrl())
			.parameter(SCOPE, scopes)
			.build();

		return url.generateUrl();
	}
}
