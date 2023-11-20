package com.moabam.api.application.notification;

import java.util.List;

import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.moabam.api.dto.notification.KnockNotificationStatusResponse;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class NotificationMapper {

	private static final String NOTIFICATION_TITLE = "모아밤";

	public static Notification toNotification(String body) {
		return Notification.builder()
			.setTitle(NOTIFICATION_TITLE)
			.setBody(body)
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
