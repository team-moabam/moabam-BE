package com.moabam.api.application.notification;

import static com.moabam.global.common.util.GlobalConstant.*;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.moabam.api.application.room.RoomService;
import com.moabam.api.domain.notification.repository.NotificationRepository;
import com.moabam.api.domain.room.Participant;
import com.moabam.api.domain.room.repository.ParticipantSearchRepository;
import com.moabam.api.dto.notification.KnockNotificationStatusResponse;
import com.moabam.api.infrastructure.fcm.FcmService;
import com.moabam.global.auth.model.AuthorizationMember;
import com.moabam.global.common.util.ClockHolder;
import com.moabam.global.error.exception.ConflictException;
import com.moabam.global.error.exception.NotFoundException;
import com.moabam.global.error.model.ErrorMessage;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

	private static final String KNOCK_BODY = "%s님이 콕 찔렀습니다.";
	private static final String CERTIFY_TIME_BODY = "%s방 인증 시간입니다.";
	private static final String KNOCK_KEY = "room_%s_member_%s_knocks_%s";

	private final FcmService fcmService;
	private final RoomService roomService;
	private final NotificationRepository notificationRepository;
	private final ParticipantSearchRepository participantSearchRepository;
	private final ClockHolder clockHolder;

	@Transactional
	public void sendKnockNotification(AuthorizationMember member, Long targetId, Long roomId) {
		roomService.validateRoomById(roomId);

		String knockKey = generateKnockKey(member.id(), targetId, roomId);
		validateConflictKnockNotification(knockKey);
		validateFcmToken(targetId);

		String fcmToken = notificationRepository.findFcmTokenByMemberId(targetId);
		String notificationBody = String.format(KNOCK_BODY, member.nickname());
		fcmService.sendAsyncFcm(fcmToken, notificationBody);
		notificationRepository.saveKnockNotification(knockKey);
	}

	@Scheduled(cron = "0 50 * * * *")
	public void sendCertificationTimeNotification() {
		int certificationTime = (clockHolder.times().getHour() + ONE_HOUR) % HOURS_IN_A_DAY;
		List<Participant> participants = participantSearchRepository.findAllByRoomCertifyTime(certificationTime);

		participants.parallelStream().forEach(participant -> {
			String roomTitle = participant.getRoom().getTitle();
			String fcmToken = notificationRepository.findFcmTokenByMemberId(participant.getMemberId());
			String notificationBody = String.format(CERTIFY_TIME_BODY, roomTitle);
			fcmService.sendAsyncFcm(fcmToken, notificationBody);
		});
	}

	/**
	 * TODO : 영명-재윤님 방 조회하실 때, 특정 사용자의 방 내 참여자들에 대한 콕 찌르기 여부를 반환해주는 메서드이니 사용하시기 바랍니다.
	 */
	public KnockNotificationStatusResponse checkMyKnockNotificationStatusInRoom(AuthorizationMember member,
		Long roomId) {
		List<Participant> participants = participantSearchRepository.findOtherParticipantsInRoom(member.id(), roomId);

		Predicate<Long> knockPredicate = targetId ->
			notificationRepository.existsByKey(generateKnockKey(member.id(), targetId, roomId));

		Map<Boolean, List<Long>> knockNotificationStatus = participants.stream()
			.map(Participant::getMemberId)
			.collect(Collectors.partitioningBy(knockPredicate));

		return NotificationMapper
			.toKnockNotificationStatusResponse(knockNotificationStatus.get(true), knockNotificationStatus.get(false));
	}

	private void validateConflictKnockNotification(String knockKey) {
		if (notificationRepository.existsByKey(knockKey)) {
			throw new ConflictException(ErrorMessage.CONFLICT_KNOCK);
		}
	}

	private void validateFcmToken(Long memberId) {
		if (!notificationRepository.existsFcmTokenByMemberId(memberId)) {
			throw new NotFoundException(ErrorMessage.NOT_FOUND_FCM_TOKEN);
		}
	}

	private String generateKnockKey(Long memberId, Long targetId, Long roomId) {
		return String.format(KNOCK_KEY, roomId, memberId, targetId);
	}
}
