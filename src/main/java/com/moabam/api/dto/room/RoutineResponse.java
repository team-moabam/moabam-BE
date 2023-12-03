package com.moabam.api.dto.room;

import lombok.Builder;

@Builder
public record RoutineResponse(
	Long routineId,
	String content
) {

}
