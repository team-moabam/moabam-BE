package com.moabam.api.application;

import static com.moabam.global.common.util.GlobalConstant.*;
import static java.util.Objects.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.moabam.api.domain.repository.NotificationRepository;
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

	@Transactional
	public void sendKnockNotification(MemberTest member, Long targetId, Long roomId) {
		String knockKey = generateKnockKey(member.memberId(), targetId, roomId);
		validateConflictKnockNotification(knockKey);
		validateFcmToken(targetId);

		String fcmToken = notificationRepository.findFcmTokenByMemberId(targetId);
		Notification notification = NotificationMapper.toKnockNotificationEntity(member.nickname());
		Message message = NotificationMapper.toMessageEntity(notification, fcmToken);

		notificationRepository.saveKnockNotification(knockKey);
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
		return requireNonNull(roomId) + UNDER_BAR + requireNonNull(memberId) + TO + requireNonNull(targetId);
	}
}
