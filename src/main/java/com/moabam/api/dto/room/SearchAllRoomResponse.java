package com.moabam.api.dto.room;

import java.util.List;

import com.moabam.api.domain.room.RoomType;

import lombok.Builder;

@Builder
public record SearchAllRoomResponse(
	Long id,
	String title,
	String image,
	boolean isPassword,
	String managerNickname,
	int level,
	RoomType roomType,
	int certifyTime,
	int currentUserCount,
	int maxUserCount,
	List<RoutineResponse> routines
) {

}
