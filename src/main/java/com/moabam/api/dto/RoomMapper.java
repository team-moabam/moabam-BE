package com.moabam.api.dto;

import com.moabam.api.domain.entity.Room;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RoomMapper {

	public static Room toRoomEntity(CreateRoomRequest createRoomRequest) {
		return Room.builder()
			.title(createRoomRequest.title())
			.password(createRoomRequest.password())
			.roomType(createRoomRequest.roomType())
			.certifyTime(createRoomRequest.certifyTime())
			.maxUserCount(createRoomRequest.maxUserCount())
			.build();
	}
}
