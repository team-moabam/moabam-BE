package com.moabam.api.application.room.mapper;

import java.time.LocalDate;
import java.util.List;

import com.moabam.api.domain.room.Participant;
import com.moabam.api.domain.room.Room;
import com.moabam.api.dto.room.CreateRoomRequest;
import com.moabam.api.dto.room.ManageRoomResponse;
import com.moabam.api.dto.room.MyRoomResponse;
import com.moabam.api.dto.room.MyRoomsResponse;
import com.moabam.api.dto.room.ParticipantResponse;
import com.moabam.api.dto.room.RoomDetailsResponse;
import com.moabam.api.dto.room.RoomHistoryResponse;
import com.moabam.api.dto.room.RoomsHistoryResponse;
import com.moabam.api.dto.room.RoutineResponse;
import com.moabam.api.dto.room.SearchAllRoomResponse;
import com.moabam.api.dto.room.SearchAllRoomsResponse;
import com.moabam.api.dto.room.TodayCertificateRankResponse;

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

	public static RoomDetailsResponse toRoomDetailsResponse(Long memberId, Room room, String managerNickname,
		List<RoutineResponse> routineResponses, List<LocalDate> certifiedDates,
		List<TodayCertificateRankResponse> todayCertificateRankResponses, double completePercentage) {
		return RoomDetailsResponse.builder()
			.roomId(room.getId())
			.myMemberId(memberId)
			.title(room.getTitle())
			.managerNickName(managerNickname)
			.roomImage(room.getRoomImage())
			.level(room.getLevel())
			.roomType(room.getRoomType())
			.certifyTime(room.getCertifyTime())
			.currentUserCount(room.getCurrentUserCount())
			.maxUserCount(room.getMaxUserCount())
			.announcement(room.getAnnouncement())
			.completePercentage(completePercentage)
			.certifiedDates(certifiedDates)
			.routines(routineResponses)
			.todayCertificateRank(todayCertificateRankResponses)
			.build();
	}

	public static MyRoomResponse toMyRoomResponse(Room room, boolean isMemberCertifiedToday,
		boolean isRoomCertifiedToday) {
		return MyRoomResponse.builder()
			.roomId(room.getId())
			.title(room.getTitle())
			.roomType(room.getRoomType())
			.certifyTime(room.getCertifyTime())
			.currentUserCount(room.getCurrentUserCount())
			.maxUserCount(room.getMaxUserCount())
			.obtainedBugs(room.getLevel())
			.isMemberCertifiedToday(isMemberCertifiedToday)
			.isRoomCertifiedToday(isRoomCertifiedToday)
			.build();
	}

	public static MyRoomsResponse toMyRoomsResponse(List<MyRoomResponse> myRoomResponses) {
		return MyRoomsResponse.builder()
			.participatingRooms(myRoomResponses)
			.build();
	}

	public static RoomHistoryResponse toRoomHistoryResponse(Long roomId, String title, Participant participant) {
		return RoomHistoryResponse.builder()
			.roomId(roomId)
			.title(title)
			.createdAt(participant.getCreatedAt())
			.deletedAt(participant.getDeletedAt())
			.build();
	}

	public static RoomsHistoryResponse toRoomsHistoryResponse(List<RoomHistoryResponse> roomHistoryResponses) {
		return RoomsHistoryResponse.builder()
			.roomHistory(roomHistoryResponses)
			.build();
	}

	public static ManageRoomResponse toManageRoomResponse(Room room, List<RoutineResponse> routines,
		List<ParticipantResponse> participantResponses) {
		return ManageRoomResponse.builder()
			.roomId(room.getId())
			.title(room.getTitle())
			.announcement(room.getAnnouncement())
			.roomType(room.getRoomType())
			.certifyTime(room.getCertifyTime())
			.maxUserCount(room.getMaxUserCount())
			.password(room.getPassword())
			.routines(routines)
			.participants(participantResponses)
			.build();
	}

	public static SearchAllRoomResponse toSearchAllRoomResponse(Room room, List<RoutineResponse> routineResponses,
		boolean isPassword) {
		return SearchAllRoomResponse.builder()
			.id(room.getId())
			.title(room.getTitle())
			.image(room.getRoomImage())
			.isPassword(isPassword)
			.managerNickname(room.getManagerNickname())
			.level(room.getLevel())
			.roomType(room.getRoomType())
			.certifyTime(room.getCertifyTime())
			.currentUserCount(room.getCurrentUserCount())
			.maxUserCount(room.getMaxUserCount())
			.routines(routineResponses)
			.build();
	}

	public static SearchAllRoomsResponse toSearchAllRoomsResponse(boolean hasNext,
		List<SearchAllRoomResponse> searchAllRoomResponses) {
		return SearchAllRoomsResponse.builder()
			.hasNext(hasNext)
			.rooms(searchAllRoomResponses)
			.build();
	}
}
