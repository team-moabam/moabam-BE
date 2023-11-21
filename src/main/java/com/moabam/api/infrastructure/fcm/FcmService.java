package com.moabam.api.infrastructure.fcm;

import org.springframework.stereotype.Service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FcmService {

	private final FirebaseMessaging firebaseMessaging;

	public void sendAsync(String fcmToken, String notificationBody) {
		Notification notification = FcmMapper.toNotification(notificationBody);

		if (fcmToken != null) {
			Message message = FcmMapper.toMessage(notification, fcmToken);
			firebaseMessaging.sendAsync(message);
		}
	}
}
