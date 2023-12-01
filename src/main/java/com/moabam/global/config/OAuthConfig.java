package com.moabam.global.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "oauth2")
public record OAuthConfig(
	Provider provider,
	Client client
) {

	public record Client(
		String provider,
		String clientId,
		String clientSecret,
		String authorizationGrantType,
		List<String> scope,
		String adminKey
	) {

	}

	public record Provider(
		String authorizationUri,
		String redirectUri,
		String tokenUri,
		String tokenInfo,
		String unlink,
		String adminRedirectUri
	) {

	}
}
