package com.moabam.api.infrastructure.fcm;

import static org.mockito.BDDMockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.moabam.global.auth.model.AuthorizationMember;
import com.moabam.global.auth.model.AuthorizationThreadLocal;
import com.moabam.global.config.FcmConfig;
import com.moabam.support.annotation.WithMember;
import com.moabam.support.common.WithoutFilterSupporter;

@SpringBootTest(classes = {FcmConfig.class, FcmService.class})
class FcmServiceTest extends WithoutFilterSupporter {

	@Autowired
	private FcmService fcmService;

	@MockBean
	private FirebaseMessaging firebaseMessaging;

	@MockBean
	private FcmRepository fcmRepository;

	@WithMember
	@DisplayName("FCM 토큰이 성공적으로 저장된다. - Void")
	@Test
	void saveToken() {
		// Given
		AuthorizationMember member = AuthorizationThreadLocal.getAuthorizationMember();

		// When
		fcmService.createToken(member, "value1");

		// Then
		verify(fcmRepository).saveToken(any(Long.class), any(String.class));
	}

	@DisplayName("FCM 토큰이 성공적으로 삭제된다. - Void")
	@Test
	void deleteTokenByMemberId() {
		// When
		fcmRepository.deleteTokenByMemberId(1L);

		// Then
		verify(fcmRepository).deleteTokenByMemberId(any(Long.class));
	}

	@DisplayName("FCM 토큰을 성공적으로 조회된다. - (String) FCM TOKEN")
	@Test
	void findTokenByMemberId() {
		// When
		fcmRepository.findTokenByMemberId(1L);

		// Then
		verify(fcmRepository).findTokenByMemberId(any(Long.class));
	}

	@DisplayName("비동기 FCM 알림을 성공적으로 보낸다. - Void")
	@Test
	void sendAsync() {
		// When
		fcmService.sendAsync("FCM-TOKEN", "알림");

		// Then
		verify(firebaseMessaging).sendAsync(any(Message.class));
	}

	@DisplayName("FCM 토큰이 null이여서 비동기 FCM 알림을 보내지 않는다. - Void")
	@Test
	void sendAsync_null() {
		// When
		fcmService.sendAsync(null, "알림");

		// Then
		verify(firebaseMessaging, times(0)).sendAsync(any(Message.class));
	}
}
