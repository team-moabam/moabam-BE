package com.moabam.api.application;

import static com.moabam.global.common.constant.FcmConstant.*;
import static com.moabam.global.common.constant.GlobalConstant.*;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.moabam.api.domain.entity.Participant;
import com.moabam.api.domain.repository.NotificationRepository;
import com.moabam.api.domain.repository.ParticipantSearchRepository;
import com.moabam.api.dto.NotificationMapper;
import com.moabam.global.common.annotation.MemberTest;
import com.moabam.global.error.exception.ConflictException;
import com.moabam.global.error.exception.NotFoundException;
import com.moabam.global.error.model.ErrorMessage;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

	private final FirebaseMessaging firebaseMessaging;
	private final NotificationRepository notificationRepository;
	private final ParticipantSearchRepository participantSearchRepository;

	@Transactional
	public void sendKnockNotification(MemberTest member, Long targetId, Long roomId) {
		String knockKey = generateKnockKey(member.memberId(), targetId, roomId);
		validateConflictKnockNotification(knockKey);
		validateFcmToken(targetId);

		Notification notification = NotificationMapper.toKnockNotificationEntity(member.nickname());
		sendAsyncFcm(targetId, notification);
		notificationRepository.saveKnockNotification(knockKey);
	}

	@Scheduled(cron = CRON_CERTIFY_TIME_EXPRESSION)
	public void sendCertificationTimeNotification() {
		int certificationTime = (LocalDateTime.now().getHour() + ONE) % HOURS_IN_A_DAY;
		List<Participant> participants = participantSearchRepository.findAllByRoomCertifyTime(certificationTime);

		participants.parallelStream().forEach(participant -> {
			String roomTitle = participant.getRoom().getTitle();
			Notification notification = NotificationMapper.toCertifyAuthNotificationEntity(roomTitle);
			sendAsyncFcm(participant.getMemberId(), notification);
		});
	}

	private void sendAsyncFcm(Long fcmTokenKey, Notification notification) {
		String fcmToken = notificationRepository.findFcmTokenByMemberId(fcmTokenKey);
		Message message = NotificationMapper.toMessageEntity(notification, fcmToken);

		firebaseMessaging.sendAsync(message);
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
