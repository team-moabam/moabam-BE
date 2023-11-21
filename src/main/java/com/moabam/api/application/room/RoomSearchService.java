package com.moabam.api.application.room;

import static com.moabam.global.common.util.GlobalConstant.*;
import static com.moabam.global.error.model.ErrorMessage.*;
import static org.apache.commons.lang3.StringUtils.*;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.moabam.api.application.member.MemberService;
import com.moabam.api.application.notification.NotificationService;
import com.moabam.api.application.room.mapper.CertificationsMapper;
import com.moabam.api.application.room.mapper.ParticipantMapper;
import com.moabam.api.application.room.mapper.RoomMapper;
import com.moabam.api.application.room.mapper.RoutineMapper;
import com.moabam.api.domain.member.Member;
import com.moabam.api.domain.room.Certification;
import com.moabam.api.domain.room.DailyMemberCertification;
import com.moabam.api.domain.room.DailyRoomCertification;
import com.moabam.api.domain.room.Participant;
import com.moabam.api.domain.room.Room;
import com.moabam.api.domain.room.RoomType;
import com.moabam.api.domain.room.Routine;
import com.moabam.api.domain.room.repository.CertificationsSearchRepository;
import com.moabam.api.domain.room.repository.ParticipantSearchRepository;
import com.moabam.api.domain.room.repository.RoomRepository;
import com.moabam.api.domain.room.repository.RoomSearchRepository;
import com.moabam.api.domain.room.repository.RoutineSearchRepository;
import com.moabam.api.dto.room.CertificationImageResponse;
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
import com.moabam.global.common.util.ClockHolder;
import com.moabam.global.error.exception.ForbiddenException;
import com.moabam.global.error.exception.NotFoundException;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomSearchService {

	private final CertificationsSearchRepository certificationsSearchRepository;
	private final ParticipantSearchRepository participantSearchRepository;
	private final RoutineSearchRepository routineSearchRepository;
	private final RoomSearchRepository roomSearchRepository;
	private final RoomRepository roomRepository;
	private final MemberService memberService;
	private final RoomCertificationService roomCertificationService;
	private final NotificationService notificationService;
	private final ClockHolder clockHolder;

	public RoomDetailsResponse getRoomDetails(Long memberId, Long roomId, LocalDate date) {
		Participant participant = participantSearchRepository.findOne(memberId, roomId)
			.orElseThrow(() -> new NotFoundException(PARTICIPANT_NOT_FOUND));
		Room room = participant.getRoom();

		String managerNickname = room.getManagerNickname();
		List<DailyMemberCertification> dailyMemberCertifications =
			certificationsSearchRepository.findSortedDailyMemberCertifications(roomId, date);
		List<RoutineResponse> routineResponses = getRoutineResponses(roomId);
		List<TodayCertificateRankResponse> todayCertificateRankResponses = getTodayCertificateRankResponses(memberId,
			roomId, dailyMemberCertifications, date);
		List<LocalDate> certifiedDates = getCertifiedDatesBeforeWeek(roomId);
		double completePercentage = calculateCompletePercentage(dailyMemberCertifications.size(),
			room.getCurrentUserCount());

		return RoomMapper.toRoomDetailsResponse(memberId, room, managerNickname, routineResponses, certifiedDates,
			todayCertificateRankResponses, completePercentage);
	}

	public MyRoomsResponse getMyRooms(Long memberId) {
		LocalDate today = clockHolder.times().toLocalDate();
		List<MyRoomResponse> myRoomResponses = new ArrayList<>();
		List<Participant> participants = participantSearchRepository.findNotDeletedParticipantsByMemberId(memberId);

		for (Participant participant : participants) {
			Room room = participant.getRoom();
			boolean isMemberCertified = roomCertificationService.existsMemberCertification(memberId, room.getId(),
				today);
			boolean isRoomCertified = roomCertificationService.existsRoomCertification(room.getId(), today);

			myRoomResponses.add(RoomMapper.toMyRoomResponse(room, isMemberCertified, isRoomCertified));
		}

		return RoomMapper.toMyRoomsResponse(myRoomResponses);
	}

	public RoomsHistoryResponse getJoinHistory(Long memberId) {
		List<Participant> participants = participantSearchRepository.findAllParticipantsByMemberId(memberId);
		List<RoomHistoryResponse> roomHistoryResponses = new ArrayList<>();

		for (Participant participant : participants) {
			if (participant.getRoom() == null) {
				roomHistoryResponses.add(RoomMapper.toRoomHistoryResponse(null,
					participant.getDeletedRoomTitle(), participant));

				continue;
			}

			roomHistoryResponses.add(RoomMapper.toRoomHistoryResponse(participant.getRoom().getId(),
				participant.getRoom().getTitle(), participant));
		}

		return RoomMapper.toRoomsHistoryResponse(roomHistoryResponses);
	}

	public ManageRoomResponse getRoomDetailsBeforeModification(Long memberId, Long roomId) {
		Participant participant = participantSearchRepository.findOne(memberId, roomId)
			.orElseThrow(() -> new NotFoundException(PARTICIPANT_NOT_FOUND));

		if (!participant.isManager()) {
			throw new ForbiddenException(ROOM_MODIFY_UNAUTHORIZED_REQUEST);
		}

		Room room = participant.getRoom();
		List<RoutineResponse> routineResponses = getRoutineResponses(roomId);
		List<Participant> participants = participantSearchRepository.findParticipantsByRoomId(roomId);
		List<Long> memberIds = participants.stream().map(Participant::getMemberId).toList();
		List<Member> members = memberService.getRoomMembers(memberIds);
		List<ParticipantResponse> participantResponses = new ArrayList<>();

		for (Member member : members) {
			int contributionPoint = calculateContributionPoint(member.getId(), participants,
				clockHolder.times().toLocalDate());

			participantResponses.add(ParticipantMapper.toParticipantResponse(member, contributionPoint));
		}

		return RoomMapper.toManageRoomResponse(room, routineResponses, participantResponses);
	}

	public SearchAllRoomsResponse searchAllRooms(@Nullable RoomType roomType, @Nullable Long roomId) {
		List<SearchAllRoomResponse> searchAllRoomResponses = new ArrayList<>();
		List<Room> rooms = new ArrayList<>(roomSearchRepository.findAllWithNoOffset(roomType, roomId));
		boolean hasNext = isHasNext(searchAllRoomResponses, rooms);

		return RoomMapper.toSearchAllRoomsResponse(hasNext, searchAllRoomResponses);
	}

	public SearchAllRoomsResponse search(String keyword, @Nullable RoomType roomType, @Nullable Long roomId) {
		List<SearchAllRoomResponse> searchAllRoomResponses = new ArrayList<>();
		List<Room> rooms = new ArrayList<>();

		if (roomId == null && roomType == null) {
			rooms = new ArrayList<>(roomRepository.searchByKeyword(keyword));
		}

		if (roomId == null && roomType != null) {
			rooms = new ArrayList<>(roomRepository.searchByKeywordAndRoomType(keyword, roomType.name()));
		}

		if (roomId != null && roomType == null) {
			rooms = new ArrayList<>(roomRepository.searchByKeywordAndRoomId(keyword, roomId));
		}

		if (roomId != null && roomType != null) {
			rooms = new ArrayList<>(
				roomRepository.searchByKeywordAndRoomIdAndRoomType(keyword, roomType.name(), roomId));
		}

		boolean hasNext = isHasNext(searchAllRoomResponses, rooms);

		return RoomMapper.toSearchAllRoomsResponse(hasNext, searchAllRoomResponses);
	}

	private boolean isHasNext(List<SearchAllRoomResponse> searchAllRoomResponses, List<Room> rooms) {
		boolean hasNext = false;

		if (rooms.size() > ROOM_FIXED_SEARCH_SIZE) {
			hasNext = true;
			rooms.remove(ROOM_FIXED_SEARCH_SIZE);
		}

		List<Long> roomIds = rooms.stream().map(Room::getId).toList();
		List<Routine> routines = routineSearchRepository.findAllByRoomIds(roomIds);

		for (Room room : rooms) {
			List<Routine> filteredRoutines = routines.stream()
				.filter(routine -> routine.getRoom().getId().equals(room.getId()))
				.toList();

			boolean isPassword = !isEmpty(room.getPassword());

			searchAllRoomResponses.add(
				RoomMapper.toSearchAllRoomResponse(room, RoutineMapper.toRoutineResponses(filteredRoutines),
					isPassword));
		}
		return hasNext;
	}

	private List<RoutineResponse> getRoutineResponses(Long roomId) {
		List<Routine> roomRoutines = routineSearchRepository.findAllByRoomId(roomId);

		return RoutineMapper.toRoutineResponses(roomRoutines);
	}

	private List<TodayCertificateRankResponse> getTodayCertificateRankResponses(Long memberId, Long roomId,
		List<DailyMemberCertification> dailyMemberCertifications, LocalDate date) {

		List<TodayCertificateRankResponse> responses = new ArrayList<>();
		List<Certification> certifications = certificationsSearchRepository.findCertifications(roomId, date);
		List<Participant> participants = participantSearchRepository.findParticipantsByRoomId(roomId);
		List<Member> members = memberService.getRoomMembers(participants.stream()
			.map(Participant::getMemberId)
			.toList());

		List<Long> myKnockedNotificationStatusInRoom = notificationService.getMyKnockedNotificationStatusInRoom(
			memberId, roomId, participants);

		addCompletedMembers(responses, dailyMemberCertifications, members, certifications, participants, date,
			myKnockedNotificationStatusInRoom);
		addUncompletedMembers(responses, dailyMemberCertifications, members, participants, date,
			myKnockedNotificationStatusInRoom);

		return responses;
	}

	private void addCompletedMembers(List<TodayCertificateRankResponse> responses,
		List<DailyMemberCertification> dailyMemberCertifications, List<Member> members,
		List<Certification> certifications, List<Participant> participants, LocalDate date,
		List<Long> myKnockedNotificationStatusInRoom) {

		int rank = 1;

		for (DailyMemberCertification certification : dailyMemberCertifications) {
			Member member = members.stream()
				.filter(m -> m.getId().equals(certification.getMemberId()))
				.findAny()
				.orElseThrow(() -> new NotFoundException(ROOM_DETAILS_ERROR));

			int contributionPoint = calculateContributionPoint(member.getId(), participants, date);
			List<CertificationImageResponse> certificationImageResponses =
				CertificationsMapper.toCertificateImageResponses(member.getId(), certifications);

			boolean isNotificationSent = myKnockedNotificationStatusInRoom.contains(member.getId());

			TodayCertificateRankResponse response = CertificationsMapper.toTodayCertificateRankResponse(
				rank, member, contributionPoint, "https://~awake", "https://~sleep", certificationImageResponses,
				isNotificationSent);

			rank += 1;
			responses.add(response);
		}
	}

	private void addUncompletedMembers(List<TodayCertificateRankResponse> responses,
		List<DailyMemberCertification> dailyMemberCertifications, List<Member> members,
		List<Participant> participants, LocalDate date, List<Long> myKnockedNotificationStatusInRoom) {

		List<Long> allMemberIds = participants.stream()
			.map(Participant::getMemberId)
			.collect(Collectors.toList());

		List<Long> certifiedMemberIds = dailyMemberCertifications.stream()
			.map(DailyMemberCertification::getMemberId)
			.toList();

		allMemberIds.removeAll(certifiedMemberIds);

		for (Long memberId : allMemberIds) {
			Member member = members.stream()
				.filter(m -> m.getId().equals(memberId))
				.findAny()
				.orElseThrow(() -> new NotFoundException(ROOM_DETAILS_ERROR));

			int contributionPoint = calculateContributionPoint(memberId, participants, date);
			boolean isNotificationSent = myKnockedNotificationStatusInRoom.contains(member.getId());

			TodayCertificateRankResponse response = CertificationsMapper.toTodayCertificateRankResponse(500, member,
				contributionPoint, "https://~awake", "https://~sleep", null, isNotificationSent);

			responses.add(response);
		}
	}

	private int calculateContributionPoint(Long memberId, List<Participant> participants, LocalDate date) {
		Participant participant = participants.stream()
			.filter(p -> p.getMemberId().equals(memberId))
			.findAny()
			.orElseThrow(() -> new NotFoundException(ROOM_DETAILS_ERROR));

		int participatedDays = Period.between(participant.getCreatedAt().toLocalDate(), date).getDays() + 1;

		return (int)(((double)participant.getCertifyCount() / participatedDays) * 100);
	}

	private List<LocalDate> getCertifiedDatesBeforeWeek(Long roomId) {
		List<DailyRoomCertification> certifications = certificationsSearchRepository.findDailyRoomCertifications(
			roomId, clockHolder.times().toLocalDate());

		return certifications.stream().map(DailyRoomCertification::getCertifiedAt).toList();
	}

	private double calculateCompletePercentage(int certifiedMembersCount, int currentsMembersCount) {
		double completePercentage = ((double)certifiedMembersCount / currentsMembersCount) * 100;

		return Math.round(completePercentage * 100) / 100.0;
	}
}
