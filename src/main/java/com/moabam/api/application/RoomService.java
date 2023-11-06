package com.moabam.api.application;

import static com.moabam.api.domain.entity.enums.RoomType.*;
import static com.moabam.global.error.model.ErrorMessage.*;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.moabam.api.domain.entity.Certification;
import com.moabam.api.domain.entity.DailyMemberCertification;
import com.moabam.api.domain.entity.DailyRoomCertification;
import com.moabam.api.domain.entity.Member;
import com.moabam.api.domain.entity.Participant;
import com.moabam.api.domain.entity.Room;
import com.moabam.api.domain.entity.Routine;
import com.moabam.api.domain.entity.enums.RoomType;
import com.moabam.api.domain.repository.CertificationsMapper;
import com.moabam.api.domain.repository.CertificationsSearchRepository;
import com.moabam.api.domain.repository.ParticipantRepository;
import com.moabam.api.domain.repository.ParticipantSearchRepository;
import com.moabam.api.domain.repository.RoomRepository;
import com.moabam.api.domain.repository.RoutineRepository;
import com.moabam.api.domain.repository.RoutineSearchRepository;
import com.moabam.api.dto.CertificationImageResponse;
import com.moabam.api.dto.CreateRoomRequest;
import com.moabam.api.dto.EnterRoomRequest;
import com.moabam.api.dto.ModifyRoomRequest;
import com.moabam.api.dto.RoomDetailsResponse;
import com.moabam.api.dto.RoomMapper;
import com.moabam.api.dto.RoutineMapper;
import com.moabam.api.dto.RoutineResponse;
import com.moabam.api.dto.TodayCertificateRankResponse;
import com.moabam.global.error.exception.BadRequestException;
import com.moabam.global.error.exception.ForbiddenException;
import com.moabam.global.error.exception.NotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomService {

	private final RoomRepository roomRepository;
	private final RoutineRepository routineRepository;
	private final RoutineSearchRepository routineSearchRepository;
	private final ParticipantRepository participantRepository;
	private final ParticipantSearchRepository participantSearchRepository;
	private final CertificationsSearchRepository certificationsSearchRepository;
	private final MemberService memberService;

	@Transactional
	public void createRoom(Long memberId, CreateRoomRequest createRoomRequest) {
		Room room = RoomMapper.toRoomEntity(createRoomRequest);
		List<Routine> routines = RoutineMapper.toRoutineEntities(room, createRoomRequest.routines());
		Participant participant = Participant.builder()
			.room(room)
			.memberId(memberId)
			.build();

		participant.enableManager();
		roomRepository.save(room);
		routineRepository.saveAll(routines);
		participantRepository.save(participant);
	}

	@Transactional
	public void modifyRoom(Long memberId, Long roomId, ModifyRoomRequest modifyRoomRequest) {
		Participant participant = getParticipant(memberId, roomId);

		if (!participant.isManager()) {
			throw new ForbiddenException(ROOM_MODIFY_UNAUTHORIZED_REQUEST);
		}

		Room room = participant.getRoom();
		room.changeTitle(modifyRoomRequest.title());
		room.changeAnnouncement(modifyRoomRequest.announcement());
		room.changePassword(modifyRoomRequest.password());
		room.changeCertifyTime(modifyRoomRequest.certifyTime());
		room.changeMaxCount(modifyRoomRequest.maxUserCount());
	}

	@Transactional
	public void enterRoom(Long memberId, Long roomId, EnterRoomRequest enterRoomRequest) {
		Room room = roomRepository.findById(roomId).orElseThrow(() -> new NotFoundException(ROOM_NOT_FOUND));
		validateRoomEnter(memberId, enterRoomRequest.password(), room);

		room.increaseCurrentUserCount();
		increaseRoomCount(memberId, room.getRoomType());

		Participant participant = Participant.builder()
			.room(room)
			.memberId(memberId)
			.build();
		participantRepository.save(participant);
	}

	@Transactional
	public void exitRoom(Long memberId, Long roomId) {
		Participant participant = getParticipant(memberId, roomId);
		Room room = participant.getRoom();

		if (participant.isManager() && room.getCurrentUserCount() != 1) {
			throw new BadRequestException(ROOM_EXIT_MANAGER_FAIL);
		}

		decreaseRoomCount(memberId, room.getRoomType());
		participant.removeRoom();
		participantRepository.delete(participant);

		if (!participant.isManager()) {
			room.decreaseCurrentUserCount();
			return;
		}

		roomRepository.flush();
		roomRepository.delete(room);
	}

	public RoomDetailsResponse getRoomDetails(Long memberId, Long roomId) {
		LocalDate today = LocalDate.now();
		Participant participant = getParticipant(memberId, roomId);
		Room room = participant.getRoom();

		String managerNickname = memberService.getManager(roomId).getNickname();
		List<DailyMemberCertification> dailyMemberCertifications =
			certificationsSearchRepository.findSortedDailyMemberCertifications(roomId, today);
		List<RoutineResponse> routineResponses = getRoutineResponses(roomId);
		List<TodayCertificateRankResponse> todayCertificateRankResponses = getTodayCertificateRankResponses(roomId,
			routineResponses, dailyMemberCertifications, today);
		List<LocalDate> certifiedDates = getCertifiedDates(roomId, today);
		double completePercentage = calculateCompletePercentage(dailyMemberCertifications.size(),
			room.getCurrentUserCount());

		return RoomMapper.toRoomDetailsResponse(room, managerNickname, routineResponses, certifiedDates,
			todayCertificateRankResponses, completePercentage);
	}

	public void validateRoomById(Long roomId) {
		if (!roomRepository.existsById(roomId)) {
			throw new NotFoundException(ROOM_NOT_FOUND);
		}
	}

	private Participant getParticipant(Long memberId, Long roomId) {
		return participantSearchRepository.findOne(memberId, roomId)
			.orElseThrow(() -> new NotFoundException(PARTICIPANT_NOT_FOUND));
	}

	private void validateRoomEnter(Long memberId, String requestPassword, Room room) {
		if (!isEnterRoomAvailable(memberId, room.getRoomType())) {
			throw new BadRequestException(MEMBER_ROOM_EXCEED);
		}
		if (!StringUtils.isEmpty(requestPassword) && !room.getPassword().equals(requestPassword)) {
			throw new BadRequestException(WRONG_ROOM_PASSWORD);
		}
		if (room.getCurrentUserCount() == room.getMaxUserCount()) {
			throw new BadRequestException(ROOM_MAX_USER_REACHED);
		}
	}

	private boolean isEnterRoomAvailable(Long memberId, RoomType roomType) {
		Member member = memberService.getById(memberId);

		if (roomType.equals(MORNING) && member.getCurrentMorningCount() >= 3) {
			return false;
		}
		if (roomType.equals(NIGHT) && member.getCurrentNightCount() >= 3) {
			return false;
		}

		return true;
	}

	private void increaseRoomCount(Long memberId, RoomType roomType) {
		Member member = memberService.getById(memberId);

		if (roomType.equals(MORNING)) {
			member.enterMorningRoom();
			return;
		}

		member.enterNightRoom();
	}

	private void decreaseRoomCount(Long memberId, RoomType roomType) {
		Member member = memberService.getById(memberId);

		if (roomType.equals(MORNING)) {
			member.exitMorningRoom();
			return;
		}

		member.exitNightRoom();
	}

	private List<RoutineResponse> getRoutineResponses(Long roomId) {
		List<Routine> roomRoutines = routineSearchRepository.findByRoomId(roomId);

		return RoutineMapper.toRoutineResponses(roomRoutines);
	}

	private List<TodayCertificateRankResponse> getTodayCertificateRankResponses(Long roomId,
		List<RoutineResponse> routines, List<DailyMemberCertification> dailyMemberCertifications, LocalDate today) {

		List<TodayCertificateRankResponse> responses = new ArrayList<>();
		List<Long> routineIds = routines.stream()
			.map(RoutineResponse::routineId)
			.toList();
		List<Certification> certifications = certificationsSearchRepository.findCertifications(
			routineIds,
			today);
		List<Participant> participants = participantSearchRepository.findParticipants(roomId);
		List<Member> members = memberService.getRoomMembers(participants.stream()
			.map(Participant::getMemberId)
			.toList());

		addCompletedMembers(responses, dailyMemberCertifications, members, certifications, participants, today);
		addUncompletedMembers(responses, dailyMemberCertifications, members, participants, today);

		return responses;
	}

	private void addCompletedMembers(List<TodayCertificateRankResponse> responses,
		List<DailyMemberCertification> dailyMemberCertifications, List<Member> members,
		List<Certification> certifications, List<Participant> participants, LocalDate today) {

		int rank = 1;

		for (DailyMemberCertification certification : dailyMemberCertifications) {
			Member member = members.stream()
				.filter(m -> m.getId().equals(certification.getMemberId()))
				.findAny()
				.orElseThrow(() -> new NotFoundException(ROOM_DETAILS_ERROR));

			int contributionPoint = calculateContributionPoint(member.getId(), participants, today);
			List<CertificationImageResponse> certificationImageResponses = CertificationsMapper.toCertificateImageResponses(
				member.getId(),
				certifications);

			TodayCertificateRankResponse response = CertificationsMapper.toTodayCertificateRankResponse(
				rank, member, contributionPoint, "https://~awake", "https://~sleep", certificationImageResponses);

			rank += 1;
			responses.add(response);
		}
	}

	private void addUncompletedMembers(List<TodayCertificateRankResponse> responses,
		List<DailyMemberCertification> dailyMemberCertifications, List<Member> members,
		List<Participant> participants, LocalDate today) {

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

			int contributionPoint = calculateContributionPoint(memberId, participants, today);

			TodayCertificateRankResponse response = CertificationsMapper.toTodayCertificateRankResponse(
				500, member, contributionPoint, "https://~awake", "https://~sleep", null);

			responses.add(response);
		}
	}

	private int calculateContributionPoint(Long memberId, List<Participant> participants, LocalDate today) {
		Participant participant = participants.stream()
			.filter(p -> p.getMemberId().equals(memberId))
			.findAny()
			.orElseThrow(() -> new NotFoundException(ROOM_DETAILS_ERROR));

		int participatedDays = Period.between(participant.getCreatedAt().toLocalDate(), today).getDays() + 1;

		return (int)(((double)participant.getCertifyCount() / participatedDays) * 100);
	}

	private List<LocalDate> getCertifiedDates(Long roomId, LocalDate today) {
		List<DailyRoomCertification> certifications = certificationsSearchRepository.findDailyRoomCertifications(
			roomId, today);

		return certifications.stream().map(DailyRoomCertification::getCertifiedAt).toList();
	}

	private double calculateCompletePercentage(int certifiedMembersCount, int currentsMembersCount) {
		double completePercentage = ((double)certifiedMembersCount / currentsMembersCount) * 100;

		return Math.round(completePercentage * 100) / 100.0;
	}
}
