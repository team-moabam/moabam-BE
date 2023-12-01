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
import com.moabam.api.domain.item.Inventory;
import com.moabam.api.domain.item.repository.InventorySearchRepository;
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
import com.moabam.api.domain.room.repository.RoutineRepository;
import com.moabam.api.dto.room.CertificationImageResponse;
import com.moabam.api.dto.room.CertificationImagesResponse;
import com.moabam.api.dto.room.GetAllRoomResponse;
import com.moabam.api.dto.room.GetAllRoomsResponse;
import com.moabam.api.dto.room.ManageRoomResponse;
import com.moabam.api.dto.room.MyRoomResponse;
import com.moabam.api.dto.room.MyRoomsResponse;
import com.moabam.api.dto.room.ParticipantResponse;
import com.moabam.api.dto.room.RoomDetailsResponse;
import com.moabam.api.dto.room.RoomHistoryResponse;
import com.moabam.api.dto.room.RoomsHistoryResponse;
import com.moabam.api.dto.room.RoutineResponse;
import com.moabam.api.dto.room.TodayCertificateRankResponse;
import com.moabam.api.dto.room.UnJoinedRoomCertificateRankResponse;
import com.moabam.api.dto.room.UnJoinedRoomDetailsResponse;
import com.moabam.global.common.util.ClockHolder;
import com.moabam.global.error.exception.ForbiddenException;
import com.moabam.global.error.exception.NotFoundException;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchService {

	private final RoomRepository roomRepository;
	private final RoomSearchRepository roomSearchRepository;
	private final RoutineRepository routineRepository;
	private final ParticipantSearchRepository participantSearchRepository;
	private final CertificationsSearchRepository certificationsSearchRepository;
	private final InventorySearchRepository inventorySearchRepository;
	private final CertificationService certificationService;
	private final MemberService memberService;
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
			roomId, dailyMemberCertifications, date, room.getRoomType());
		List<LocalDate> certifiedDates = getCertifiedDatesBeforeWeek(roomId);
		double completePercentage = calculateCompletePercentage(dailyMemberCertifications.size(),
			room.getCurrentUserCount());

		return RoomMapper.toRoomDetailsResponse(memberId, room, managerNickname, routineResponses, certifiedDates,
			todayCertificateRankResponses, completePercentage);
	}

	public MyRoomsResponse getMyRooms(Long memberId) {
		LocalDate today = clockHolder.date();
		List<MyRoomResponse> myRoomResponses = new ArrayList<>();
		List<Participant> participants = participantSearchRepository.findNotDeletedParticipantsByMemberId(memberId);

		for (Participant participant : participants) {
			Room room = participant.getRoom();
			boolean isMemberCertified = certificationService.existsMemberCertification(memberId, room.getId(), today);
			boolean isRoomCertified = certificationService.existsRoomCertification(room.getId(), today);

			myRoomResponses.add(RoomMapper.toMyRoomResponse(room, isMemberCertified, isRoomCertified));
		}

		return RoomMapper.toMyRoomsResponse(myRoomResponses);
	}

	public RoomsHistoryResponse getJoinHistory(Long memberId) {
		List<Participant> participants = participantSearchRepository.findAllParticipantsByMemberId(memberId);
		List<RoomHistoryResponse> roomHistoryResponses = participants.stream()
			.map(participant -> {
				if (participant.getRoom() == null) {
					return RoomMapper.toRoomHistoryResponse(null, participant.getDeletedRoomTitle(), participant);
				}

				Room room = participant.getRoom();

				return RoomMapper.toRoomHistoryResponse(room.getId(), room.getTitle(), participant);
			})
			.toList();

		return RoomMapper.toRoomsHistoryResponse(roomHistoryResponses);
	}

	public ManageRoomResponse getRoomForModification(Long memberId, Long roomId) {
		Participant participant = participantSearchRepository.findOne(memberId, roomId)
			.orElseThrow(() -> new NotFoundException(PARTICIPANT_NOT_FOUND));

		if (!participant.isManager()) {
			throw new ForbiddenException(ROOM_MODIFY_UNAUTHORIZED_REQUEST);
		}

		Room room = participant.getRoom();
		List<RoutineResponse> routineResponses = getRoutineResponses(roomId);
		List<Participant> participants = participantSearchRepository.findParticipantsByRoomId(roomId);
		List<Long> memberIds = participants.stream()
			.map(Participant::getMemberId)
			.toList();
		List<Member> members = memberService.getRoomMembers(memberIds);
		List<ParticipantResponse> participantResponses = new ArrayList<>();

		for (Member member : members) {
			int contributionPoint = calculateContributionPoint(member.getId(), participants, clockHolder.date());

			participantResponses.add(ParticipantMapper.toParticipantResponse(member, contributionPoint));
		}

		return RoomMapper.toManageRoomResponse(room, memberId, routineResponses, participantResponses);
	}

	public GetAllRoomsResponse getAllRooms(@Nullable RoomType roomType, @Nullable Long roomId) {
		List<GetAllRoomResponse> getAllRoomResponse = new ArrayList<>();
		List<Room> rooms = new ArrayList<>(roomSearchRepository.findAllWithNoOffset(roomType, roomId));
		boolean hasNext = isHasNext(getAllRoomResponse, rooms);

		return RoomMapper.toSearchAllRoomsResponse(hasNext, getAllRoomResponse);
	}

	// TODO: full-text search 로 바꾸면서 리팩토링 예정
	public GetAllRoomsResponse searchRooms(String keyword, @Nullable RoomType roomType, @Nullable Long roomId) {
		List<GetAllRoomResponse> getAllRoomResponse = new ArrayList<>();
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

		boolean hasNext = isHasNext(getAllRoomResponse, rooms);

		return RoomMapper.toSearchAllRoomsResponse(hasNext, getAllRoomResponse);
	}

	public UnJoinedRoomDetailsResponse getUnJoinedRoomDetails(Long roomId) {
		Room room = roomRepository.findById(roomId)
			.orElseThrow(() -> new NotFoundException(ROOM_NOT_FOUND));

		List<Routine> routines = routineRepository.findAllByRoomId(roomId);
		List<RoutineResponse> routineResponses = RoutineMapper.toRoutineResponses(routines);
		List<DailyMemberCertification> sortedDailyMemberCertifications =
			certificationsSearchRepository.findSortedDailyMemberCertifications(roomId, clockHolder.date());
		List<Long> memberIds = sortedDailyMemberCertifications.stream()
			.map(DailyMemberCertification::getMemberId)
			.toList();
		List<Member> members = memberService.getRoomMembers(memberIds);
		List<Inventory> inventories = inventorySearchRepository.findDefaultInventories(memberIds,
			room.getRoomType().name());
		List<UnJoinedRoomCertificateRankResponse> unJoinedRoomCertificateRankResponses = new ArrayList<>();

		int rank = 1;
		for (DailyMemberCertification certification : sortedDailyMemberCertifications) {
			Member member = members.stream()
				.filter(m -> m.getId().equals(certification.getMemberId()))
				.findAny()
				.orElseThrow(() -> new NotFoundException(MEMBER_NOT_FOUND));

			Inventory inventory = inventories.stream()
				.filter(i -> i.getMemberId().equals(member.getId()))
				.findAny()
				.orElseThrow(() -> new NotFoundException(INVENTORY_NOT_FOUND));

			UnJoinedRoomCertificateRankResponse response = RoomMapper.toUnJoinedRoomCertificateRankResponse(member,
				rank, inventory);

			unJoinedRoomCertificateRankResponses.add(response);
			rank += 1;
		}

		return RoomMapper.toUnJoinedRoomDetails(room, routineResponses, unJoinedRoomCertificateRankResponses);
	}

	private boolean isHasNext(List<GetAllRoomResponse> getAllRoomResponse, List<Room> rooms) {
		boolean hasNext = false;

		if (rooms.size() > ROOM_FIXED_SEARCH_SIZE) {
			hasNext = true;
			rooms.remove(ROOM_FIXED_SEARCH_SIZE);
		}

		List<Long> roomIds = rooms.stream()
			.map(Room::getId)
			.toList();
		List<Routine> routines = routineRepository.findAllByRoomIdIn(roomIds);

		for (Room room : rooms) {
			List<Routine> filteredRoutines = routines.stream()
				.filter(routine -> routine.getRoom().getId().equals(room.getId()))
				.toList();
			List<RoutineResponse> filteredResponses = RoutineMapper.toRoutineResponses(filteredRoutines);
			boolean isPassword = !isEmpty(room.getPassword());

			getAllRoomResponse.add(RoomMapper.toSearchAllRoomResponse(room, filteredResponses, isPassword));
		}

		return hasNext;
	}

	private List<RoutineResponse> getRoutineResponses(Long roomId) {
		List<Routine> roomRoutines = routineRepository.findAllByRoomId(roomId);

		return RoutineMapper.toRoutineResponses(roomRoutines);
	}

	private List<TodayCertificateRankResponse> getTodayCertificateRankResponses(Long memberId, Long roomId,
		List<DailyMemberCertification> dailyMemberCertifications, LocalDate date, RoomType roomType) {

		List<TodayCertificateRankResponse> responses = new ArrayList<>();
		List<Certification> certifications = certificationsSearchRepository.findCertifications(roomId, date);
		List<Participant> participants = participantSearchRepository.findParticipantsByRoomId(roomId);
		List<Member> members = memberService.getRoomMembers(participants.stream()
			.map(Participant::getMemberId)
			.toList());

		List<Long> knocks = notificationService.getMyKnockStatusInRoom(memberId, roomId, participants);

		List<Long> memberIds = members.stream()
			.map(Member::getId)
			.toList();
		List<Inventory> inventories = inventorySearchRepository.findDefaultInventories(memberIds, roomType.name());

		responses.addAll(completedMembers(dailyMemberCertifications, members, certifications, participants, date,
			knocks, inventories));
		responses.addAll(uncompletedMembers(dailyMemberCertifications, members, participants, date, knocks,
			inventories));

		return responses;
	}

	private List<TodayCertificateRankResponse> completedMembers(
		List<DailyMemberCertification> dailyMemberCertifications, List<Member> members,
		List<Certification> certifications, List<Participant> participants, LocalDate date, List<Long> knocks,
		List<Inventory> inventories) {

		List<TodayCertificateRankResponse> responses = new ArrayList<>();
		int rank = 1;

		for (DailyMemberCertification certification : dailyMemberCertifications) {
			Member member = members.stream()
				.filter(m -> m.getId().equals(certification.getMemberId()))
				.findAny()
				.orElseThrow(() -> new NotFoundException(ROOM_DETAILS_ERROR));

			Inventory inventory = inventories.stream()
				.filter(i -> i.getMemberId().equals(member.getId()))
				.findAny()
				.orElseThrow(() -> new NotFoundException(INVENTORY_NOT_FOUND));

			String awakeImage = inventory.getItem().getAwakeImage();
			String sleepImage = inventory.getItem().getSleepImage();

			int contributionPoint = calculateContributionPoint(member.getId(), participants, date);
			CertificationImagesResponse certificationImages = getCertificationImages(member.getId(), certifications);
			boolean isNotificationSent = knocks.contains(member.getId());

			TodayCertificateRankResponse response = CertificationsMapper.toTodayCertificateRankResponse(rank, member,
				contributionPoint, awakeImage, sleepImage, certificationImages, isNotificationSent);

			rank += 1;
			responses.add(response);
		}

		return responses;
	}

	private List<TodayCertificateRankResponse> uncompletedMembers(
		List<DailyMemberCertification> dailyMemberCertifications, List<Member> members, List<Participant> participants,
		LocalDate date, List<Long> knocks, List<Inventory> inventories) {

		List<TodayCertificateRankResponse> responses = new ArrayList<>();

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

			Inventory inventory = inventories.stream()
				.filter(i -> i.getMemberId().equals(member.getId()))
				.findAny()
				.orElseThrow(() -> new NotFoundException(INVENTORY_NOT_FOUND));

			String awakeImage = inventory.getItem().getAwakeImage();
			String sleepImage = inventory.getItem().getSleepImage();

			int contributionPoint = calculateContributionPoint(memberId, participants, date);
			boolean isNotificationSent = knocks.contains(member.getId());

			TodayCertificateRankResponse response = CertificationsMapper.toTodayCertificateRankResponse(
				NOT_COMPLETED_RANK, member, contributionPoint, awakeImage, sleepImage, null,
				isNotificationSent);

			responses.add(response);
		}

		return responses;
	}

	private CertificationImagesResponse getCertificationImages(Long memberId, List<Certification> certifications) {
		List<CertificationImageResponse> certificationImageResponses = certifications.stream()
			.filter(certification -> certification.getMemberId().equals(memberId))
			.map(certification -> CertificationsMapper.toCertificateImageResponse(certification.getRoutine().getId(),
				certification.getImage()))
			.toList();

		return CertificationsMapper.toCertificateImagesResponse(certificationImageResponses);
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
			roomId, clockHolder.date());

		return certifications.stream()
			.map(DailyRoomCertification::getCertifiedAt)
			.toList();
	}

	private double calculateCompletePercentage(int certifiedMembersCount, int currentsMembersCount) {
		double completePercentage = ((double)certifiedMembersCount / currentsMembersCount) * 100;

		return Math.round(completePercentage * 100) / 100.0;
	}
}
