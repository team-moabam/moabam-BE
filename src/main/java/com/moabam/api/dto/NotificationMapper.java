package com.moabam.api.dto;

import java.util.List;

import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class NotificationMapper {

	private static final String NOTIFICATION_TITLE = "모아밤";
	private static final String KNOCK_BODY = "님이 콕 찔렀습니다.";
	private static final String CERTIFY_TIME_BODY = "방 인증 시간입니다.";

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

	public static KnockNotificationStatusResponse toKnockNotificationStatusResponse(
		List<Long> knockedMembersId,
		List<Long> notKnockedMembersId
	) {
		return KnockNotificationStatusResponse.builder()
			.knockedMembersId(knockedMembersId)
			.notKnockedMembersId(notKnockedMembersId)
			.build();
	}
}
