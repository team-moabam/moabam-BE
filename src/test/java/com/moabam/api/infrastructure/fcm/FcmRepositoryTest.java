package com.moabam.api.infrastructure.fcm;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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
class FcmRepositoryTest {

	@InjectMocks
	FcmRepository fcmRepository;

	@Mock
	ValueRedisRepository valueRedisRepository;

	@DisplayName("FCM 토큰이 성공적으로 저장된다. - Void")
	@Test
	void saveToken_success() {
		// When
		fcmRepository.saveToken("FCM-TOKEN", 1L);

		// Then
		verify(valueRedisRepository).save(any(String.class), any(String.class), any(Duration.class));
	}

	@DisplayName("ID가 Null인 사용자가 FCM 토큰을 저장한다. - NullPointerException")
	@Test
	void saveToken_MemberId_NullPointerException() {
		// When & Then
		assertThatThrownBy(() -> fcmRepository.saveToken("FCM-TOKEN", null))
			.isInstanceOf(NullPointerException.class);
	}

	@DisplayName("토큰이 Null인 FCM 토큰을 저장한다. - NullPointerException")
	@Test
	void saveToken_FcmToken_NullPointerException() {
		// When & Then
		assertThatThrownBy(() -> fcmRepository.saveToken(null, 1L))
			.isInstanceOf(NullPointerException.class);
	}

	@DisplayName("FCM 토큰이 성공적으로 삭제된다. - Void")
	@Test
	void deleteTokenByMemberId_success() {
		// When
		fcmRepository.deleteTokenByMemberId(1L);

		// Then
		verify(valueRedisRepository).delete(any(String.class));
	}

	@DisplayName("ID가 Null인 사용자가 FCM 토큰을 삭제한다. - NullPointerException")
	@Test
	void deleteTokenByMemberId_NullPointerException() {
		// When & Then
		assertThatThrownBy(() -> fcmRepository.deleteTokenByMemberId(null))
			.isInstanceOf(NullPointerException.class);
	}

	@DisplayName("FCM 토큰을 성공적으로 조회된다. - (String) FCM TOKEN")
	@Test
	void findTokenByMemberId_success() {
		// When
		fcmRepository.findTokenByMemberId(1L);

		// Then
		verify(valueRedisRepository).get(any(String.class));
	}

	@DisplayName("ID가 Null인 사용자가 FCM 토큰을 조회한다. - NullPointerException")
	@Test
	void findTokenByMemberId_NullPointerException() {
		// When & Then
		assertThatThrownBy(() -> fcmRepository.findTokenByMemberId(null))
			.isInstanceOf(NullPointerException.class);
	}
}
