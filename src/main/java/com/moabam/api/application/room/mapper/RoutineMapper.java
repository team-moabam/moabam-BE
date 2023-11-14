package com.moabam.api.application.room.mapper;

import java.util.List;

import com.moabam.api.domain.room.Room;
import com.moabam.api.domain.room.Routine;
import com.moabam.api.dto.room.RoutineResponse;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RoutineMapper {

	public static List<Routine> toRoutineEntities(Room room, List<String> routinesRequest) {
		return routinesRequest.stream()
			.map(routine -> Routine.builder()
				.room(room)
				.content(routine)
				.build())
			.toList();
	}

	public static List<RoutineResponse> toRoutineResponses(List<Routine> routines) {
		return routines.stream()
			.map(routine -> RoutineResponse.builder()
				.routineId(routine.getId())
				.content(routine.getContent())
				.build())
			.toList();
	}
}
