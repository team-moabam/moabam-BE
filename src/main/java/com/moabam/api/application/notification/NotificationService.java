package com.moabam.api.application.notification;

import static com.moabam.global.common.util.GlobalConstant.*;

import java.time.LocalDateTime;
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
import com.moabam.api.infrastructure.fcm.FcmService;
import com.moabam.global.auth.model.AuthorizationMember;
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
		int certificationTime = (LocalDateTime.now().getHour() + ONE_HOUR) % HOURS_IN_A_DAY;
		List<Participant> participants = participantSearchRepository.findAllByRoomCertifyTime(certificationTime);

		participants.parallelStream().forEach(participant -> {
			String roomTitle = participant.getRoom().getTitle();
			String fcmToken = notificationRepository.findFcmTokenByMemberId(participant.getMemberId());
			String notificationBody = String.format(CERTIFY_TIME_BODY, roomTitle);
			fcmService.sendAsyncFcm(fcmToken, notificationBody);
		});
	}

	public List<Long> getMyKnockedNotificationStatusInRoom(Long memberId, Long roomId,
		List<Participant> participants) {
		List<Participant> filteredParticipants = participants.stream()
			.filter(participant -> !participant.getMemberId().equals(memberId))
			.toList();

		Predicate<Long> knockPredicate = targetId ->
			notificationRepository.existsByKey(generateKnockKey(memberId, targetId, roomId));

		Map<Boolean, List<Long>> knockNotificationStatus = filteredParticipants.stream()
			.map(Participant::getMemberId)
			.collect(Collectors.partitioningBy(knockPredicate));

		return knockNotificationStatus.get(true);
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
