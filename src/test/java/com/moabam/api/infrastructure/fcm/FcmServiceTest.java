package com.moabam.api.infrastructure.fcm;

import static org.mockito.BDDMockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.moabam.global.config.FcmConfig;

@SpringBootTest(classes = {FcmConfig.class, FcmService.class})
class FcmServiceTest {

	@Autowired
	FcmService fcmService;

	@MockBean
	FirebaseMessaging firebaseMessaging;

	@MockBean
	FcmRepository fcmRepository;

	@DisplayName("FCM 토큰이 성공적으로 저장된다. - Void")
	@Test
	void saveToken_success() {
		// When
		fcmService.createToken("FCM-TOKEN", 1L);

		// Then
		verify(fcmRepository).saveToken(any(String.class), any(Long.class));
	}

	@DisplayName("FCM 토큰으로 빈값이 넘어와 아무일도 일어나지 않는다. - Void")
	@Test
	void saveToken_Blank() {
		// When
		fcmService.createToken("", 1L);

		// Then
		verify(fcmRepository, times(0)).saveToken(any(String.class), any(Long.class));
	}

	@DisplayName("FCM 토큰으로 null이 넘어와 아무일도 일어나지 않는다. - Void")
	@Test
	void saveToken_Null() {
		// When
		fcmService.createToken(null, 1L);

		// Then
		verify(fcmRepository, times(0)).saveToken(any(String.class), any(Long.class));
	}

	@DisplayName("FCM 토큰이 성공적으로 삭제된다. - Void")
	@Test
	void deleteTokenByMemberId_success() {
		// When
		fcmRepository.deleteTokenByMemberId(1L);

		// Then
		verify(fcmRepository).deleteTokenByMemberId(any(Long.class));
	}

	@DisplayName("FCM 토큰을 성공적으로 조회된다. - (String) FCM TOKEN")
	@Test
	void findTokenByMemberId_success() {
		// When
		fcmRepository.findTokenByMemberId(1L);

		// Then
		verify(fcmRepository).findTokenByMemberId(any(Long.class));
	}

	@DisplayName("비동기 FCM 알림을 성공적으로 보낸다. - Void")
	@Test
	void sendAsync_success() {
		// When
		fcmService.sendAsync("FCM-TOKEN", "title", "body");

		// Then
		verify(firebaseMessaging).sendAsync(any(Message.class));
	}

	@DisplayName("FCM 토큰이 null이여서 비동기 FCM 알림을 보내지 않는다. - Void")
	@Test
	void sendAsync_null() {
		// When
		fcmService.sendAsync(null, "titile", "body");

		// Then
		verify(firebaseMessaging, times(0)).sendAsync(any(Message.class));
	}
}
