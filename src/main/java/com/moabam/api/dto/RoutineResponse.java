package com.moabam.api.dto;

import lombok.Builder;

@Builder
public record RoutineResponse(
	Long routineId,
	String content
) {

}
