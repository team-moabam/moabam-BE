package com.moabam.api.infrastructure.fcm;

import static org.mockito.Mockito.*;

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
	private FcmService fcmService;

	@MockBean
	private FirebaseMessaging firebaseMessaging;

	@DisplayName("비동기 FCM 알림을 성공적으로 보낸다. - Void")
	@Test
	void fcmService_sendAsyncFcm() {
		// When
		fcmService.sendAsyncFcm("FCM-TOKEN", "알림");

		// Then
		verify(firebaseMessaging).sendAsync(any(Message.class));
	}

	@DisplayName("FCM 토큰이 null이여서 비동기 FCM 알림을 보내지. - Void")
	@Test
	void fcmService_sendAsyncFcm_null() {
		// When
		fcmService.sendAsyncFcm(null, "알림");

		// Then
		verify(firebaseMessaging, times(0)).sendAsync(any(Message.class));
	}
}
