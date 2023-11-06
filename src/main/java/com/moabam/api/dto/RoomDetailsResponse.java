package com.moabam.api.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.Builder;

@Builder
public record RoomDetailsResponse(
	Long roomId,
	String title,
	String managerNickName,
	String roomImage,
	int level,
	String roomType,
	int certifyTime,
	int currentUserCount,
	int maxUserCount,
	String announcement,
	double completePercentage,
	List<LocalDate> certifiedDates,
	List<RoutineResponse> routine,
	List<TodayCertificateRankResponse> todayCertificateRank
) {

}
