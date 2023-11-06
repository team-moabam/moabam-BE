package com.moabam.fixture;

import com.moabam.api.domain.entity.Room;
import com.moabam.api.domain.entity.enums.RoomType;

public class RoomFixture {

	public static Room room() {
		return Room.builder()
			.title("testTitle")
			.roomType(RoomType.MORNING)
			.certifyTime(10)
			.maxUserCount(8)
			.build();
	}
}
