package com.moabam.api.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.moabam.api.domain.repository.NotificationRepository;
import com.moabam.global.common.annotation.MemberTest;
import com.moabam.global.error.exception.ConflictException;
import com.moabam.global.error.exception.NotFoundException;
import com.moabam.global.error.model.ErrorMessage;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

	@InjectMocks
	private NotificationService notificationService;

	@Mock
	private NotificationRepository notificationRepository;

	@Mock
	private FirebaseMessaging firebaseMessaging;

	private MemberTest memberTest;

	@BeforeEach
	void setUp() {
		memberTest = new MemberTest(2L, "nickname");
	}

	@DisplayName("성공적으로 상대를 콕 찔렀을 때, - Void")
	@Test
	void notificationService_sendKnockNotification() {
		// Given
		given(notificationRepository.existsFcmTokenByMemberId(any(Long.class))).willReturn(true);
		given(notificationRepository.existsKnockByMemberId(any(Long.class), any(Long.class), any(Long.class)))
			.willReturn(false);
		given(notificationRepository.findFcmTokenByMemberId(any(Long.class))).willReturn("FCM-TOKEN");

		// When
		notificationService.sendKnockNotification(memberTest, 2L, 1L);

		// Then
		verify(firebaseMessaging).sendAsync(any(Message.class));
		verify(notificationRepository).saveKnockNotification(any(Long.class), any(Long.class), any(Long.class));
	}

	@DisplayName("콕 찌를 상대의 FCM 토큰이 존재하지 않을 때, - NotFoundException")
	@Test
	void notificationService_sendKnockNotification_NotFoundException() {
		// Given
		given(notificationRepository.existsFcmTokenByMemberId(any(Long.class))).willReturn(false);

		// When & Then
		assertThatThrownBy(() -> notificationService.sendKnockNotification(memberTest, 1L, 1L))
			.isInstanceOf(NotFoundException.class)
			.hasMessage(ErrorMessage.FCM_TOKEN_NOT_FOUND.getMessage());
	}

	@DisplayName("콕 찌를 상대가 이미 찌른 상대일 때, - ConflictException")
	@Test
	void notificationService_sendKnockNotification_ConflictException() {
		// Given
		given(notificationRepository.existsFcmTokenByMemberId(any(Long.class))).willReturn(true);
		given(notificationRepository.existsKnockByMemberId(any(Long.class), any(Long.class), any(Long.class)))
			.willReturn(true);

		// When & Then
		assertThatThrownBy(() -> notificationService.sendKnockNotification(memberTest, 1L, 1L))
			.isInstanceOf(ConflictException.class)
			.hasMessage(ErrorMessage.KNOCK_CONFLICT.getMessage());
	}
}
