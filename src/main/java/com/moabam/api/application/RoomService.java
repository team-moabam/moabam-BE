package com.moabam.api.application;

import static com.moabam.api.domain.entity.enums.RoomType.*;
import static com.moabam.api.domain.resizedimage.ImageType.*;
import static com.moabam.global.error.model.ErrorMessage.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.moabam.api.domain.entity.Certification;
import com.moabam.api.domain.entity.DailyMemberCertification;
import com.moabam.api.domain.entity.DailyRoomCertification;
import com.moabam.api.domain.entity.Member;
import com.moabam.api.domain.entity.Participant;
import com.moabam.api.domain.entity.Room;
import com.moabam.api.domain.entity.Routine;
import com.moabam.api.domain.entity.enums.BugType;
import com.moabam.api.domain.entity.enums.RequireExp;
import com.moabam.api.domain.entity.enums.RoomType;
import com.moabam.api.domain.repository.CertificationRepository;
import com.moabam.api.domain.repository.CertificationsSearchRepository;
import com.moabam.api.domain.repository.DailyMemberCertificationRepository;
import com.moabam.api.domain.repository.DailyRoomCertificationRepository;
import com.moabam.api.domain.repository.ParticipantRepository;
import com.moabam.api.domain.repository.ParticipantSearchRepository;
import com.moabam.api.domain.repository.RoomRepository;
import com.moabam.api.domain.repository.RoutineRepository;
import com.moabam.api.domain.repository.RoutineSearchRepository;
import com.moabam.api.dto.CertificationImageResponse;
import com.moabam.api.dto.CertificationsMapper;
import com.moabam.api.dto.CreateRoomRequest;
import com.moabam.api.dto.EnterRoomRequest;
import com.moabam.api.dto.ModifyRoomRequest;
import com.moabam.api.dto.RoomDetailsResponse;
import com.moabam.api.dto.RoomMapper;
import com.moabam.api.dto.RoutineMapper;
import com.moabam.api.dto.RoutineResponse;
import com.moabam.api.dto.TodayCertificateRankResponse;
import com.moabam.global.common.util.ClockHolder;
import com.moabam.global.common.util.UrlSubstringParser;
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
	private final DailyMemberCertificationRepository dailyMemberCertificationRepository;
	private final DailyRoomCertificationRepository dailyRoomCertificationRepository;
	private final CertificationRepository certificationRepository;
	private final MemberService memberService;
	private final ImageService imageService;
	private final ClockHolder clockHolder;

	@Transactional
	public Long createRoom(Long memberId, CreateRoomRequest createRoomRequest) {
		Room room = RoomMapper.toRoomEntity(createRoomRequest);
		List<Routine> routines = RoutineMapper.toRoutineEntities(room, createRoomRequest.routines());
		Participant participant = Participant.builder()
			.room(room)
			.memberId(memberId)
			.build();

		participant.enableManager();
		Room savedRoom = roomRepository.save(room);
		routineRepository.saveAll(routines);
		participantRepository.save(participant);

		return savedRoom.getId();
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

		List<Routine> routines = routineSearchRepository.findByRoomId(roomId);
		routineRepository.deleteAll(routines);

		List<Routine> newRoutines = RoutineMapper.toRoutineEntities(room, modifyRoomRequest.routines());
		routineRepository.saveAll(newRoutines);
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
			dailyMemberCertifications, today);
		List<LocalDate> certifiedDates = getCertifiedDates(roomId, today);
		double completePercentage = calculateCompletePercentage(dailyMemberCertifications.size(),
			room.getCurrentUserCount());

		return RoomMapper.toRoomDetailsResponse(room, managerNickname, routineResponses, certifiedDates,
			todayCertificateRankResponses, completePercentage);
	}

	@Transactional
	public void certifyRoom(Long memberId, Long roomId, List<MultipartFile> multipartFiles) {
		LocalDate today = LocalDate.now();
		Participant participant = getParticipant(memberId, roomId);
		Room room = participant.getRoom();
		Member member = memberService.getById(memberId);
		BugType bugType = switch (room.getRoomType()) {
			case MORNING -> BugType.MORNING;
			case NIGHT -> BugType.NIGHT;
		};
		int roomLevel = room.getLevel();

		validateCertifyTime(clockHolder.times(), room.getCertifyTime());
		validateAlreadyCertified(memberId, roomId, today);

		DailyMemberCertification dailyMemberCertification = CertificationsMapper.toDailyMemberCertification(memberId,
			roomId, participant);
		dailyMemberCertificationRepository.save(dailyMemberCertification);

		member.increaseTotalCertifyCount();

		List<String> result = imageService.uploadImages(multipartFiles, CERTIFICATION);
		saveNewCertifications(result, memberId);

		Optional<DailyRoomCertification> dailyRoomCertification =
			certificationsSearchRepository.findDailyRoomCertification(roomId, today);

		if (dailyRoomCertification.isEmpty()) {
			List<DailyMemberCertification> dailyMemberCertifications =
				certificationsSearchRepository.findSortedDailyMemberCertifications(roomId, today);
			double completePercentage = calculateCompletePercentage(dailyMemberCertifications.size(),
				room.getCurrentUserCount());

			if (completePercentage >= 75) {
				DailyRoomCertification createDailyRoomCertification = CertificationsMapper.toDailyRoomCertification(
					roomId, today);

				dailyRoomCertificationRepository.save(createDailyRoomCertification);

				int expAppliedRoomLevel = getRoomLevelAfterExpApply(roomLevel, room);

				List<Long> memberIds = dailyMemberCertifications.stream()
					.map(DailyMemberCertification::getMemberId)
					.toList();

				memberService.getRoomMembers(memberIds)
					.forEach(completedMember -> completedMember.getBug().increaseBug(bugType, expAppliedRoomLevel));

				return;
			}
		}

		if (dailyRoomCertification.isPresent()) {
			member.getBug().increaseBug(bugType, roomLevel);
		}
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
		List<DailyMemberCertification> dailyMemberCertifications, LocalDate today) {

		List<TodayCertificateRankResponse> responses = new ArrayList<>();
		List<Certification> certifications = certificationsSearchRepository.findCertifications(roomId, today);
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
			List<CertificationImageResponse> certificationImageResponses =
				CertificationsMapper.toCertificateImageResponses(member.getId(), certifications);

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

	private void validateCertifyTime(LocalDateTime now, int certifyTime) {
		LocalTime targetTime = LocalTime.of(certifyTime, 0);
		LocalDateTime minusTenMinutes = LocalDateTime.of(now.toLocalDate(), targetTime).minusMinutes(10);
		LocalDateTime plusTenMinutes = LocalDateTime.of(now.toLocalDate(), targetTime).plusMinutes(10);

		if (now.isBefore(minusTenMinutes) || now.isAfter(plusTenMinutes)) {
			throw new BadRequestException(INVALID_CERTIFY_TIME);
		}
	}

	private void validateAlreadyCertified(Long memberId, Long roomId, LocalDate today) {
		if (certificationsSearchRepository.findDailyMemberCertification(memberId, roomId, today).isPresent()) {
			throw new BadRequestException(DUPLICATED_DAILY_MEMBER_CERTIFICATION);
		}
	}

	private void saveNewCertifications(List<String> imageUrls, Long memberId) {
		List<Certification> certifications = new ArrayList<>();

		for (String imageUrl : imageUrls) {
			Long routineId = Long.parseLong(UrlSubstringParser.parseUrl(imageUrl, "_"));
			Routine routine = routineRepository.findById(routineId).orElseThrow(() -> new NotFoundException(
				ROUTINE_NOT_FOUND));

			Certification certification = CertificationsMapper.toCertification(routine, memberId, imageUrl);
			certifications.add(certification);
		}

		certificationRepository.saveAll(certifications);
	}

	private int getRoomLevelAfterExpApply(int roomLevel, Room room) {
		int requireExp = RequireExp.of(roomLevel).getTotalExp();
		room.gainExp();

		if (room.getExp() == requireExp) {
			room.levelUp();
		}

		return room.getLevel();
	}
}
