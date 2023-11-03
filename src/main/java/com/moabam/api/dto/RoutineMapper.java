package com.moabam.api.dto;

import java.util.List;

import com.moabam.api.domain.entity.Room;
import com.moabam.api.domain.entity.Routine;

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
}
