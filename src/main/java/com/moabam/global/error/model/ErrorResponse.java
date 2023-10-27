package com.moabam.global.error.model;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

public record ErrorResponse(
	String message,
	@JsonInclude(JsonInclude.Include.NON_EMPTY) Map<String, String> validation
) {
}
