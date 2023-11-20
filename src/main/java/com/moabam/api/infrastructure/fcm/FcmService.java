package com.moabam.api.infrastructure.fcm;

import org.springframework.stereotype.Service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.moabam.api.application.notification.NotificationMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FcmService {

	private final FirebaseMessaging firebaseMessaging;

	public void sendAsyncFcm(String fcmToken, String notificationBody) {
		Notification notification = NotificationMapper.toNotification(notificationBody);

		if (fcmToken != null) {
			Message message = NotificationMapper.toMessageEntity(notification, fcmToken);
			firebaseMessaging.sendAsync(message);
		}
	}
}
