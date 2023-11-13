package com.moabam.api.dto.auth;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.moabam.global.auth.model.PublicClaim;

import lombok.Builder;

@Builder
public record LoginResponse(
	boolean isSignUp,
	@JsonUnwrapped PublicClaim publicClaim
) {

}
