package com.moabam.api.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AuthorizationTokenResponse(
	@JsonProperty("token_type") String tokenType,
	@JsonProperty("access_token") String accessToken,
	@JsonProperty("id_token") String idToken,
	@JsonProperty("expires_in") String expiresIn,
	@JsonProperty("refresh_token") String refreshToken,
	@JsonProperty("refresh_token_expires_in") String refreshTokenExpiresIn,
	@JsonProperty("scope") String scope
) {

}
