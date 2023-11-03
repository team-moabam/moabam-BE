package com.moabam.api.application;

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
		validateFcmToken(targetId);
		validateConflictKnockNotification(member.memberId(), targetId, roomId);

		String fcmToken = notificationRepository.findFcmTokenByMemberId(targetId);
		Notification notification = NotificationMapper.toKnockNotificationEntity(member.nickname());
		Message message = NotificationMapper.toMessageEntity(notification, fcmToken);

		firebaseMessaging.sendAsync(message);
		notificationRepository.saveKnockNotification(member.memberId(), targetId, roomId);
	}

	private void validateFcmToken(Long memberId) {
		if (!notificationRepository.existsFcmTokenByMemberId(memberId)) {
			throw new NotFoundException(ErrorMessage.FCM_TOKEN_NOT_FOUND);
		}
	}

	private void validateConflictKnockNotification(Long memberId, Long targetId, Long roomId) {
		if (notificationRepository.existsKnockByMemberId(memberId, targetId, roomId)) {
			throw new ConflictException(ErrorMessage.KNOCK_CONFLICT);
		}
	}
}
