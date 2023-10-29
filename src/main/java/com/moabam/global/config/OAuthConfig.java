package com.moabam.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "oauth2")
public record OAuthConfig(
	Provider provider,
	Client client
) {

	public record Client(
		String provider,
		String clientId,
		String authorizationGrantType,
		List<String> scope
	) {

	}

	public record Provider(
		String authorizationUrl,
		String redirectUrl
	) {

	}
}
