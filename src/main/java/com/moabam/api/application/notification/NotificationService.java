package com.moabam.api.application.notification;

import static com.moabam.global.common.util.GlobalConstant.*;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.moabam.api.application.member.MemberService;
import com.moabam.api.application.room.RoomService;
import com.moabam.api.domain.notification.repository.NotificationRepository;
import com.moabam.api.domain.room.Participant;
import com.moabam.api.domain.room.repository.ParticipantSearchRepository;
import com.moabam.api.infrastructure.fcm.FcmService;
import com.moabam.global.common.util.ClockHolder;
import com.moabam.global.error.exception.ConflictException;
import com.moabam.global.error.exception.NotFoundException;
import com.moabam.global.error.model.ErrorMessage;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

	private static final String COMMON_TITLE = "모아밤";
	private static final String KNOCK_BODY = "[%s] - [%s]님이 콕콕콕!";
	private static final String CERTIFY_TIME_BODY = "[%s] - 인증 시간!";

	private final ClockHolder clockHolder;
	private final FcmService fcmService;
	private final RoomService roomService;
	private final MemberService memberService;

	private final NotificationRepository notificationRepository;
	private final ParticipantSearchRepository participantSearchRepository;

	@Transactional
	public void sendKnock(Long roomId, Long targetId, Long memberId) {
		validateConflictKnock(roomId, targetId, memberId);
		String fcmToken = fcmService.findTokenByMemberId(targetId)
			.orElseThrow(() -> new NotFoundException(ErrorMessage.NOT_FOUND_FCM_TOKEN));

		String roomTitle = roomService.findRoom(roomId).getTitle();
		String memberNickname = memberService.findMember(memberId).getNickname();
		String notificationTitle = roomId.toString();

		String notificationBody = String.format(KNOCK_BODY, roomTitle, memberNickname);
		fcmService.sendAsync(fcmToken, notificationTitle, notificationBody);
		notificationRepository.saveKnock(roomId, targetId, memberId);
	}

	public void sendCouponIssueResult(Long memberId, String couponName, String body) {
		String fcmToken = fcmService.findTokenByMemberId(memberId).orElse(null);
		String notificationBody = String.format(body, couponName);
		fcmService.sendAsync(fcmToken, COMMON_TITLE, notificationBody);
	}

	@Scheduled(cron = "0 50 * * * *")
	public void sendCertificationTime() {
		int certificationTime = (clockHolder.times().getHour() + ONE_HOUR) % HOURS_IN_A_DAY;
		List<Participant> participants = participantSearchRepository.findAllByRoomCertifyTime(certificationTime);

		participants.parallelStream().forEach(participant -> {
			String roomTitle = participant.getRoom().getTitle();
			String notificationTitle = participant.getRoom().getId().toString();
			String notificationBody = String.format(CERTIFY_TIME_BODY, roomTitle);
			String fcmToken = fcmService.findTokenByMemberId(participant.getMemberId()).orElse(null);
			fcmService.sendAsync(fcmToken, notificationTitle, notificationBody);
		});
	}

	public List<Long> getMyKnockStatusInRoom(Long memberId, Long roomId, List<Participant> participants) {
		List<Participant> filteredParticipants = participants.stream()
			.filter(participant -> !participant.getMemberId().equals(memberId))
			.toList();

		Predicate<Long> knockPredicate = targetId ->
			notificationRepository.existsKnockByKey(roomId, targetId, memberId);

		Map<Boolean, List<Long>> knockStatus = filteredParticipants.stream()
			.map(Participant::getMemberId)
			.collect(Collectors.partitioningBy(knockPredicate));

		return knockStatus.get(true);
	}

	private void validateConflictKnock(Long roomId, Long targetId, Long memberId) {
		if (notificationRepository.existsKnockByKey(roomId, targetId, memberId)) {
			throw new ConflictException(ErrorMessage.CONFLICT_KNOCK);
		}
	}
}
