package com.moabam.api.infrastructure.fcm;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmService {

	private final FirebaseMessaging firebaseMessaging;
	private final FcmRepository fcmRepository;

	public void createToken(String fcmToken, Long memberId) {
		if (fcmToken == null || fcmToken.isBlank()) {
			return;
		}

		log.info("FCM TOKEN before: " + fcmToken);
		fcmRepository.saveToken(fcmToken, memberId);
		log.info("FCM TOKEN after: " + findTokenByMemberId(memberId));
	}

	public void deleteTokenByMemberId(Long memberId) {
		fcmRepository.deleteTokenByMemberId(memberId);
	}

	public Optional<String> findTokenByMemberId(Long targetId) {
		return Optional.ofNullable(fcmRepository.findTokenByMemberId(targetId));
	}

	public void sendAsync(String fcmToken, String notificationBody) {
		Notification notification = FcmMapper.toNotification(notificationBody);

		if (fcmToken != null) {
			Message message = FcmMapper.toMessage(notification, fcmToken);
			firebaseMessaging.sendAsync(message);
		}
	}
}
