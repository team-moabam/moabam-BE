package com.moabam.api.dto.room;

import java.util.List;

import com.moabam.api.domain.room.RoomType;

import lombok.Builder;

@Builder
public record UnJoinedRoomDetailsResponse(
	Long roomId,
	boolean isPassword,
	String title,
	String roomImage,
	int level,
	int exp,
	RoomType roomType,
	int certifyTime,
	int currentUserCount,
	int maxUserCount,
	String announcement,
	List<RoutineResponse> routines,
	List<UnJoinedRoomCertificateRankResponse> certifiedRanks
) {

}
