package com.moabam.api.domain.notification.repository;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Duration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.moabam.api.infrastructure.redis.StringRedisRepository;

@ExtendWith(MockitoExtension.class)
class NotificationRepositoryTest {

	@InjectMocks
	private NotificationRepository notificationRepository;

	@Mock
	private StringRedisRepository stringRedisRepository;

	@DisplayName("FCM 토큰이 성공적으로 저장된다. - Void")
	@Test
	void notificationRepository_saveFcmToken() {
		// When
		notificationRepository.saveFcmToken(1L, "value1");

		// Then
		verify(stringRedisRepository).save(any(String.class), any(String.class), any(Duration.class));
	}

	@DisplayName("FCM 토큰 저장 시, 필요한 값이 NULL 이다. - NullPointerException")
	@Test
	void notificationRepository_save_NullPointerException() {
		// When & Then
		assertThatThrownBy(() -> notificationRepository.saveFcmToken(null, "value"))
			.isInstanceOf(NullPointerException.class);
	}

	@DisplayName("콕 알림이 성공적으로 저장된다. - Void")
	@Test
	void notificationRepository_saveKnockNotification() {
		// When
		notificationRepository.saveKnock("knockKey");

		// Then
		verify(stringRedisRepository).save(any(String.class), any(String.class), any(Duration.class));
	}

	@DisplayName("콕 알림 저장 시, 필요한 값이 NULL 이다. - NullPointerException")
	@Test
	void notificationRepository_saveKnockNotification_NullPointerException() {
		// When & Then
		assertThatThrownBy(() -> notificationRepository.saveKnock(null))
			.isInstanceOf(NullPointerException.class);
	}

	@DisplayName("FCM 토큰이 성공적으로 삭제된다. - Void")
	@Test
	void notificationRepository_deleteFcmTokenByMemberId() {
		// When
		notificationRepository.deleteFcmTokenByMemberId(1L);

		// Then
		verify(stringRedisRepository).delete(any(String.class));
	}

	@DisplayName("FCM 토큰 삭제 시, 필요한 값이 NULL 이다. - NullPointerException")
	@Test
	void notificationRepository_deleteFcmTokenByMemberId_NullPointerException() {
		// When & Then
		assertThatThrownBy(() -> notificationRepository.deleteFcmTokenByMemberId(null))
			.isInstanceOf(NullPointerException.class);
	}

	@DisplayName("FCM 토큰을 성공적으로 조회된다. - (String) FCM TOKEN")
	@Test
	void notificationRepository_findFcmTokenByMemberId() {
		// When
		notificationRepository.findFcmTokenByMemberId(1L);

		// Then
		verify(stringRedisRepository).get(any(String.class));
	}

	@DisplayName("FCM 토큰 조회 시, 필요한 값이 NULL 이다. - NullPointerException")
	@Test
	void notificationRepository_findFcmTokenByMemberId_NullPointerException() {
		// When & Then
		assertThatThrownBy(() -> notificationRepository.findFcmTokenByMemberId(null))
			.isInstanceOf(NullPointerException.class);
	}

	@DisplayName("FCM 토큰 존재 여부를 성공적으로 확인한다. - Boolean")
	@Test
	void notificationRepository_existsFcmTokenByMemberId() {
		// When
		notificationRepository.existsFcmTokenByMemberId(1L);

		// Then
		verify(stringRedisRepository).hasKey(any(String.class));
	}

	@DisplayName("FCM 토큰 존재 여부 체크 시, 필요한 값이 NULL 이다. - NullPointerException")
	@Test
	void notificationRepository_existsFcmTokenByMemberId_NullPointerException() {
		// When & Then
		assertThatThrownBy(() -> notificationRepository.existsFcmTokenByMemberId(null))
			.isInstanceOf(NullPointerException.class);
	}

	@DisplayName("콕 알림 여부 체크를 정상적으로 확인한다. - Boolean")
	@Test
	void notificationRepository_existsKnockByMemberId() {
		// When
		notificationRepository.existsKnockByKnockKey("knock key");

		// Then
		verify(stringRedisRepository).hasKey(any(String.class));
	}

	@DisplayName("콕 알림 여부 체크 시, 필요한 값이 NULL 이다. - NullPointerException")
	@Test
	void notificationRepository_existsKnockByMemberId_NullPointerException() {
		// When & Then
		assertThatThrownBy(() -> notificationRepository.existsKnockByKnockKey(null))
			.isInstanceOf(NullPointerException.class);
	}
}
