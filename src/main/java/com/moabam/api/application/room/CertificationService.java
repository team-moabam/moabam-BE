package com.moabam.api.application.room;

import static com.moabam.global.common.util.GlobalConstant.*;
import static com.moabam.global.error.model.ErrorMessage.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.moabam.api.application.bug.BugService;
import com.moabam.api.application.member.BadgeService;
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
import com.moabam.api.dto.room.CertifiedMemberInfo;
import com.moabam.global.common.util.ClockHolder;
import com.moabam.global.common.util.UrlSubstringParser;
import com.moabam.global.error.exception.BadRequestException;
import com.moabam.global.error.exception.NotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CertificationService {

	private static final int REQUIRED_ROOM_CERTIFICATION = 75;

	private final RoutineRepository routineRepository;
	private final CertificationRepository certificationRepository;
	private final ParticipantSearchRepository participantSearchRepository;
	private final CertificationsSearchRepository certificationsSearchRepository;
	private final DailyRoomCertificationRepository dailyRoomCertificationRepository;
	private final DailyMemberCertificationRepository dailyMemberCertificationRepository;
	private final MemberService memberService;
	private final BadgeService badgeService;
	private final BugService bugService;
	private final ClockHolder clockHolder;

	@Transactional
	public CertifiedMemberInfo getCertifiedMemberInfo(Long memberId, Long roomId, List<String> imageUrls) {
		LocalDate today = clockHolder.date();
		Participant participant = participantSearchRepository.findOne(memberId, roomId)
			.orElseThrow(() -> new NotFoundException(PARTICIPANT_NOT_FOUND));
		Room room = participant.getRoom();
		Member member = memberService.findMember(memberId);
		BugType bugType = switch (room.getRoomType()) {
			case MORNING -> BugType.MORNING;
			case NIGHT -> BugType.NIGHT;
		};

		validateCertifyTime(clockHolder.dateTime(), room.getCertifyTime());
		validateAlreadyCertified(memberId, roomId, today);

		certifyMember(memberId, roomId, participant, member, imageUrls);

		return CertificationsMapper.toCertifiedMemberInfo(today, bugType, room, member);
	}

	@Transactional
	public void certifyRoom(CertifiedMemberInfo certifyInfo) {
		LocalDate date = certifyInfo.date();
		BugType bugType = certifyInfo.bugType();
		Room room = certifyInfo.room();
		Member member = certifyInfo.member();

		Optional<DailyRoomCertification> dailyRoomCertification =
			certificationsSearchRepository.findDailyRoomCertification(room.getId(), date);

		if (dailyRoomCertification.isEmpty()) {
			certifyRoomIfAvailable(room.getId(), date, room, bugType, room.getLevel());
			return;
		}

		bugService.reward(member, bugType, room.getLevel());
	}

	public boolean existsMemberCertification(Long memberId, Long roomId, LocalDate date) {
		return dailyMemberCertificationRepository.existsByMemberIdAndRoomIdAndCreatedAtBetween(memberId, roomId,
			date.atStartOfDay(), date.atTime(LocalTime.MAX));
	}

	public boolean existsRoomCertification(Long roomId, LocalDate date) {
		return dailyRoomCertificationRepository.existsByRoomIdAndCertifiedAt(roomId, date);
	}

	public boolean existsAnyMemberCertification(Long roomId, LocalDate date) {
		return dailyMemberCertificationRepository.existsByRoomIdAndCreatedAtBetween(roomId, date.atStartOfDay(),
			date.atTime(LocalTime.MAX));
	}

	public Certification findCertification(Long certificationId) {
		return certificationRepository.findById(certificationId)
			.orElseThrow(() -> new NotFoundException(CERTIFICATION_NOT_FOUND));
	}

	private void validateCertifyTime(LocalDateTime now, int certifyTime) {
		LocalTime targetTime = LocalTime.of(certifyTime, 0);
		LocalDateTime targetDateTime = LocalDateTime.of(now.toLocalDate(), targetTime);

		if (certifyTime == MIDNIGHT_HOUR && now.getHour() != MIDNIGHT_HOUR) {
			targetDateTime = targetDateTime.plusDays(1);
		}

		LocalDateTime plusTenMinutes = targetDateTime.plusMinutes(10);

		if (now.isBefore(targetDateTime) || now.isAfter(plusTenMinutes)) {
			throw new BadRequestException(INVALID_CERTIFY_TIME);
		}
	}

	private void validateAlreadyCertified(Long memberId, Long roomId, LocalDate today) {
		if (certificationsSearchRepository.findDailyMemberCertification(memberId, roomId, today).isPresent()) {
			throw new BadRequestException(DUPLICATED_DAILY_MEMBER_CERTIFICATION);
		}
	}

	private void certifyMember(Long memberId, Long roomId, Participant participant, Member member, List<String> urls) {
		DailyMemberCertification dailyMemberCertification = CertificationsMapper.toDailyMemberCertification(memberId,
			roomId, participant);
		dailyMemberCertificationRepository.save(dailyMemberCertification);
		member.increaseTotalCertifyCount();
		badgeService.createBadge(member.getId(), member.getTotalCertifyCount());
		participant.updateCertifyCount();

		saveNewCertifications(memberId, urls);
	}

	private void saveNewCertifications(Long memberId, List<String> imageUrls) {
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

	private void certifyRoomIfAvailable(Long roomId, LocalDate today, Room room, BugType bugType, int roomLevel) {
		List<DailyMemberCertification> dailyMemberCertifications =
			certificationsSearchRepository.findSortedDailyMemberCertifications(roomId, today);
		double completePercentage = calculateCompletePercentage(dailyMemberCertifications.size(),
			room.getCurrentUserCount());

		if (completePercentage >= REQUIRED_ROOM_CERTIFICATION) {
			DailyRoomCertification createDailyRoomCertification = CertificationsMapper.toDailyRoomCertification(
				roomId, today);

			dailyRoomCertificationRepository.save(createDailyRoomCertification);
			int expAppliedRoomLevel = getRoomLevelAfterExpApply(roomLevel, room);

			provideBugToCompletedMembers(bugType, dailyMemberCertifications, expAppliedRoomLevel);
		}
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

	private void provideBugToCompletedMembers(BugType bugType, List<DailyMemberCertification> dailyMemberCertifications,
		int expAppliedRoomLevel) {
		List<Long> memberIds = dailyMemberCertifications.stream()
			.map(DailyMemberCertification::getMemberId)
			.toList();

		memberService.getRoomMembers(memberIds)
			.forEach(completedMember -> bugService.reward(completedMember, bugType, expAppliedRoomLevel));
	}
}
