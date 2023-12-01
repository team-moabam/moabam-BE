package com.moabam.api.dto.room;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.moabam.api.domain.room.RoomType;

import lombok.Builder;

@Builder
public record RoomDetailsResponse(
	Long roomId,
	LocalDateTime roomCreatedAt,
	Long myMemberId,
	String title,
	String managerNickName,
	String roomImage,
	int level,
	int exp,
	RoomType roomType,
	int certifyTime,
	int currentUserCount,
	int maxUserCount,
	String announcement,
	double completePercentage,
	List<LocalDate> certifiedDates,
	List<RoutineResponse> routines,
	List<TodayCertificateRankResponse> todayCertificateRank
) {

}
