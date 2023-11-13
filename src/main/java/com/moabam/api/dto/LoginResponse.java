package com.moabam.api.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import lombok.Builder;

@Builder
public record LoginResponse(
	boolean isSignUp,
	@JsonUnwrapped PublicClaim publicClaim
) {

}
