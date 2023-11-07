package com.moabam.api.dto;

import java.time.LocalDate;
import java.util.List;

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

	public static RoomDetailsResponse toRoomDetailsResponse(Room room, String managerNickname,
		List<RoutineResponse> routineResponses, List<LocalDate> certifiedDates,
		List<TodayCertificateRankResponse> todayCertificateRankResponses, double completePercentage) {
		return RoomDetailsResponse.builder()
			.roomId(room.getId())
			.title(room.getTitle())
			.managerNickName(managerNickname)
			.roomImage(room.getRoomImage())
			.level(room.getLevel())
			.roomType(room.getRoomType().name())
			.certifyTime(room.getCertifyTime())
			.currentUserCount(room.getCurrentUserCount())
			.maxUserCount(room.getMaxUserCount())
			.announcement(room.getAnnouncement())
			.completePercentage(completePercentage)
			.certifiedDates(certifiedDates)
			.routine(routineResponses)
			.todayCertificateRank(todayCertificateRankResponses)
			.build();
	}
}
