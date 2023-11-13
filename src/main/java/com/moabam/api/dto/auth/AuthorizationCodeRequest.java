package com.moabam.api.dto.auth;

import static java.util.Objects.*;

import java.util.List;

import lombok.Builder;

public record AuthorizationCodeRequest(
	String clientId,
	String redirectUri,
	String responseType,
	List<String> scope,
	String state
) {

	@Builder
	public AuthorizationCodeRequest(String clientId, String redirectUri, String responseType, List<String> scope,
		String state) {
		this.clientId = requireNonNull(clientId);
		this.redirectUri = requireNonNull(redirectUri);
		this.responseType = responseType;
		this.scope = scope;
		this.state = state;
	}
}
