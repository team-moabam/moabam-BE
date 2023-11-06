package com.moabam.api.presentation;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.moabam.api.application.NotificationService;
import com.moabam.global.common.annotation.MemberTest;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notifications")
public class NotificationController {

	private final NotificationService notificationService;

	@GetMapping("/rooms/{roomId}/members/{memberId}")
	public void sendKnockNotification(@PathVariable Long roomId, @PathVariable Long memberId) {
		notificationService.sendKnockNotification(new MemberTest(1L, "nickname"), memberId, roomId);
	}
}
