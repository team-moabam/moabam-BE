package com.moabam.api.dto;

import java.util.List;

import com.moabam.api.domain.entity.Room;
import com.moabam.api.domain.entity.Routine;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class RoomMapper {

	public static Room toRoomEntity(CreateRoomRequest createRoomRequest) {
		return Room.builder()
			.title(createRoomRequest.title())
			.password(createRoomRequest.password())
			.roomType(createRoomRequest.roomType())
			.certifyTime(createRoomRequest.certifyTime())
			.maxUserCount(createRoomRequest.maxUserCount())
			.build();
	}

	public static List<Routine> toRoutineEntity(Room room, List<String> routinesRequest) {
		return routinesRequest.stream()
			.map(routine -> Routine.builder()
				.room(room)
				.content(routine)
				.build())
			.toList();
	}
}
