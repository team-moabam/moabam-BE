package com.moabam.api.dto;

import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NotificationMapper {

	private static final String TITLE = "모아밤";
	private static final String KNOCK_BODY = "님이 콕 찔렀습니다.";

	public static Notification toKnockNotificationEntity(Long memberId) {
		return Notification.builder()
			.setTitle(TITLE)
			.setBody(memberId + KNOCK_BODY)
			.build();
	}

	public static Message toMessageEntity(Notification notification, String fcmToken) {
		return Message.builder()
			.setNotification(notification)
			.setToken(fcmToken)
			.build();
	}
}
