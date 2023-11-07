package com.moabam.fixture;

import com.moabam.api.domain.entity.Room;
import com.moabam.api.domain.entity.enums.RoomType;

public final class RoomFixture {

	public static Room room() {
		return Room.builder()
			.title("testTitle")
			.roomType(RoomType.MORNING)
			.certifyTime(10)
			.maxUserCount(8)
			.build();
	}

	public static Room room(int certifyTime) {
		return Room.builder()
			.title("testTitle")
			.roomType(RoomType.MORNING)
			.certifyTime(certifyTime)
			.maxUserCount(8)
			.build();
	}
}
