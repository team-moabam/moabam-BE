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

import com.moabam.api.infrastructure.redis.ValueRedisRepository;

@ExtendWith(MockitoExtension.class)
class NotificationRepositoryTest {

	@InjectMocks
	NotificationRepository notificationRepository;

	@Mock
	ValueRedisRepository valueRedisRepository;

	@DisplayName("콕 알림이 성공적으로 저장된다. - Void")
	@Test
	void saveKnock_success() {
		// When
		notificationRepository.saveKnock(1L, 1L, 1L);

		// Then
		verify(valueRedisRepository).save(any(String.class), any(String.class), any(Duration.class));
	}

	@DisplayName("콕 찌르는 사용자의 ID가 Null인 콕 알림을 저장한다. - NullPointerException")
	@Test
	void saveKnock_MemberId_NullPointerException() {
		// When & Then
		assertThatThrownBy(() -> notificationRepository.saveKnock(null, 1L, 1L))
			.isInstanceOf(NullPointerException.class);
	}

	@DisplayName("콕 찌를 대상의 ID가 Null인 콕 알림을 저장한다. - NullPointerException")
	@Test
	void saveKnock_TargetId_NullPointerException() {
		// When & Then
		assertThatThrownBy(() -> notificationRepository.saveKnock(1L, null, 1L))
			.isInstanceOf(NullPointerException.class);
	}

	@DisplayName("방 ID가 Null인 콕 알림을 저장한다. - NullPointerException")
	@Test
	void saveKnock_RoomId_NullPointerException() {
		// When & Then
		assertThatThrownBy(() -> notificationRepository.saveKnock(1L, 2L, null))
			.isInstanceOf(NullPointerException.class);
	}

	@DisplayName("콕 알림 여부 체크를 성공적으로 확인한다. - Boolean")
	@Test
	void existsKnockByKey_success() {
		// When
		notificationRepository.existsKnockByKey(1L, 1L, 1L);

		// Then
		verify(valueRedisRepository).hasKey(any(String.class));
	}

	@DisplayName("콕 찌르는 사용자의 ID가 Null인 콕 알림 여부를 체크한다. - NullPointerException")
	@Test
	void existsKnockByKey_MemberId_NullPointerException() {
		// When & Then
		assertThatThrownBy(() -> notificationRepository.existsKnockByKey(null, 1L, 1L))
			.isInstanceOf(NullPointerException.class);
	}

	@DisplayName("콕 찌를 상대 ID가 Null인 콕 알림 여부를 체크한다. - NullPointerException")
	@Test
	void existsKnockByKey_TargetId_NullPointerException() {
		// When & Then
		assertThatThrownBy(() -> notificationRepository.existsKnockByKey(1L, null, 1L))
			.isInstanceOf(NullPointerException.class);
	}

	@DisplayName("방 ID가 Null인 콕 알림 여부를 체크한다. - NullPointerException")
	@Test
	void existsKnockByKey_RoomId_NullPointerException() {
		// When & Then
		assertThatThrownBy(() -> notificationRepository.existsKnockByKey(1L, 2L, null))
			.isInstanceOf(NullPointerException.class);
	}
}
