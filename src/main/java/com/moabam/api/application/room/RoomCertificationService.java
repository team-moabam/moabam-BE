package com.moabam.api.application.room;

import static com.moabam.api.domain.image.ImageType.*;
import static com.moabam.global.error.model.ErrorMessage.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.moabam.api.application.image.ImageService;
import com.moabam.api.application.member.MemberService;
import com.moabam.api.application.room.mapper.CertificationsMapper;
import com.moabam.api.domain.bug.BugType;
import com.moabam.api.domain.member.Member;
import com.moabam.api.domain.room.Certification;
import com.moabam.api.domain.room.DailyMemberCertification;
import com.moabam.api.domain.room.DailyRoomCertification;
import com.moabam.api.domain.room.Participant;
import com.moabam.api.domain.room.Room;
import com.moabam.api.domain.room.RoomExp;
import com.moabam.api.domain.room.Routine;
import com.moabam.api.domain.room.repository.CertificationRepository;
import com.moabam.api.domain.room.repository.CertificationsSearchRepository;
import com.moabam.api.domain.room.repository.DailyMemberCertificationRepository;
import com.moabam.api.domain.room.repository.DailyRoomCertificationRepository;
import com.moabam.api.domain.room.repository.ParticipantSearchRepository;
import com.moabam.api.domain.room.repository.RoutineRepository;
import com.moabam.global.common.util.ClockHolder;
import com.moabam.global.common.util.UrlSubstringParser;
import com.moabam.global.error.exception.BadRequestException;
import com.moabam.global.error.exception.NotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomCertificationService {

	private final RoutineRepository routineRepository;
	private final CertificationRepository certificationRepository;
	private final ParticipantSearchRepository participantSearchRepository;
	private final CertificationsSearchRepository certificationsSearchRepository;
	private final DailyRoomCertificationRepository dailyRoomCertificationRepository;
	private final DailyMemberCertificationRepository dailyMemberCertificationRepository;
	private final MemberService memberService;
	private final ImageService imageService;
	private final ClockHolder clockHolder;

	@Transactional
	public void certifyRoom(Long memberId, Long roomId, List<MultipartFile> multipartFiles) {
		LocalDate today = LocalDate.now();
		Participant participant = participantSearchRepository.findOne(memberId, roomId)
			.orElseThrow(() -> new NotFoundException(PARTICIPANT_NOT_FOUND));
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

	private double calculateCompletePercentage(int certifiedMembersCount, int currentsMembersCount) {
		double completePercentage = ((double)certifiedMembersCount / currentsMembersCount) * 100;

		return Math.round(completePercentage * 100) / 100.0;
	}

	private int getRoomLevelAfterExpApply(int roomLevel, Room room) {
		int requireExp = RoomExp.of(roomLevel).getTotalExp();
		room.gainExp();

		if (room.getExp() == requireExp) {
			room.levelUp();
		}

		return room.getLevel();
	}
}
