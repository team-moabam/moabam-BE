package com.moabam.api.dto.room;

import com.moabam.api.domain.room.RoomType;

import lombok.Builder;

@Builder
public record MyRoomResponse(
	Long roomId,
	String title,
	RoomType roomType,
	int certifyTime,
	int currentUserCount,
	int maxUserCount,
	int obtainedBugs,
	boolean isMemberCertifiedToday,
	boolean isRoomCertifiedToday
) {

}
