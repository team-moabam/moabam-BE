package com.moabam.api.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AuthorizationTokenInfoResponse(
	@JsonProperty("id") long id,
	@JsonProperty("expires_in") String expiresIn,
	@JsonProperty("app_id") String appId
) {

}
