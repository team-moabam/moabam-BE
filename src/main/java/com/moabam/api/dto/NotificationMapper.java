package com.moabam.api.dto;

import static com.moabam.global.common.constant.FcmConstant.*;

import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class NotificationMapper {

	public static Notification toKnockNotificationEntity(String nickname) {
		return Notification.builder()
			.setTitle(NOTIFICATION_TITLE)
			.setBody(nickname + KNOCK_BODY)
			.build();
	}

	public static Notification toCertifyAuthNotificationEntity(String title) {
		return Notification.builder()
			.setTitle(NOTIFICATION_TITLE)
			.setBody(title + CERTIFY_TIME_BODY)
			.build();
	}

	public static Message toMessageEntity(Notification notification, String fcmToken) {
		return Message.builder()
			.setNotification(notification)
			.setToken(fcmToken)
			.build();
	}
}
