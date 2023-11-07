package com.moabam.api.dto;

import lombok.Builder;

@Builder
public record LoginResponse(
	Long id,
	boolean isSignUp
) {

}
