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

	private final NotificationRepository notificationRepository;

	@Transactional
	public void sendKnockNotification(Long targetId, MemberTest member) {
		validateFcmToken(targetId);
		validateKnockNotification(member.memberId(), targetId);
		String fcmToken = notificationRepository.findFcmTokenByMemberId(targetId);
		Notification notification = NotificationMapper.toKnockNotificationEntity(member.memberId());
		Message message = NotificationMapper.toMessageEntity(notification, fcmToken);
		FirebaseMessaging.getInstance().sendAsync(message);
		notificationRepository.saveKnockNotification(member.memberId(), targetId);
	}

	private void validateFcmToken(Long memberId) {
		if (!notificationRepository.existsFcmTokenByMemberId(memberId)) {
			throw new NotFoundException(ErrorMessage.FCM_TOKEN_NOT_FOUND);
		}
	}

	private void validateKnockNotification(Long memberId, Long targetId) {
		if (!notificationRepository.existsKnockByMemberId(memberId, targetId)) {
			throw new ConflictException(ErrorMessage.KNOCK_CONFLICT);
		}
	}
}
