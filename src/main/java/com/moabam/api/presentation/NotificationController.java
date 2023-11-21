package com.moabam.api.presentation;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.moabam.api.application.notification.NotificationService;
import com.moabam.global.auth.annotation.Auth;
import com.moabam.global.auth.model.AuthMember;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notifications")
public class NotificationController {

	private final NotificationService notificationService;

	@GetMapping("/rooms/{roomId}/members/{memberId}")
	public void sendKnockNotification(@Auth AuthMember member, @PathVariable("roomId") Long roomId,
		@PathVariable("memberId") Long memberId) {
		notificationService.sendKnock(member, memberId, roomId);
	}
}
