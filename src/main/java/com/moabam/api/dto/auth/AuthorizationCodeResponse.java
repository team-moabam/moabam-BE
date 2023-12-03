package com.moabam.api.dto.auth;

public record AuthorizationCodeResponse(
	String code,
	String error,
	String errorDescription,
	String state
) {

}
