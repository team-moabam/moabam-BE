package com.moabam.api.dto;

public record AuthorizationCodeResponse(
	String code,
	String error,
	String errorDescription,
	String state
) {

}
