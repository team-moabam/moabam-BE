package com.moabam.api.infrastructure.fcm;

import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FcmMapper {

	private static final String NOTIFICATION_TITLE = "모아밤";

	public static Notification toNotification(String body) {
		return Notification.builder()
			.setTitle(NOTIFICATION_TITLE)
			.setBody(body)
			.build();
	}

	public static Message toMessage(Notification notification, String fcmToken) {
		return Message.builder()
			.setNotification(notification)
			.setToken(fcmToken)
			.build();
	}
}
