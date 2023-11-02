package com.moabam.api.dto;

import static java.util.Objects.*;

import lombok.Builder;

public record AuthorizationTokenRequest(
	String grantType,
	String clientId,
	String redirectUri,
	String code,
	String clientSecret
) {

	@Builder
	public AuthorizationTokenRequest(String grantType, String clientId, String redirectUri, String code,
		String clientSecret) {
		this.grantType = requireNonNull(grantType);
		this.clientId = requireNonNull(clientId);
		this.redirectUri = requireNonNull(redirectUri);
		this.code = requireNonNull(code);
		this.clientSecret = clientSecret;
	}
}
