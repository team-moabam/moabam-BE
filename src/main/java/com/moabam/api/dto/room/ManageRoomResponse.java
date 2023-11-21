package com.moabam.api.dto.room;

import java.util.List;

import com.moabam.api.domain.room.RoomType;

import lombok.Builder;

@Builder
public record ManageRoomResponse(
	Long roomId,
	String title,
	String announcement,
	RoomType roomType,
	int certifyTime,
	int maxUserCount,
	String password,
	List<RoutineResponse> routines,
	List<ParticipantResponse> participants
) {

}
