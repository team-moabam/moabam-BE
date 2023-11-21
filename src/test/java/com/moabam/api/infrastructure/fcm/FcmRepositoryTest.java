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

import com.moabam.api.infrastructure.redis.StringRedisRepository;

@ExtendWith(MockitoExtension.class)
class FcmRepositoryTest {

	@InjectMocks
	private FcmRepository fcmRepository;

	@Mock
	private StringRedisRepository stringRedisRepository;

	@DisplayName("FCM 토큰이 성공적으로 저장된다. - Void")
	@Test
	void saveToken() {
		// When
		fcmRepository.saveToken(1L, "value1");

		// Then
		verify(stringRedisRepository).save(any(String.class), any(String.class), any(Duration.class));
	}

	@DisplayName("FCM 토큰 저장 시, 필요한 값이 NULL 이다. - NullPointerException")
	@Test
	void saveToken_NullPointerException() {
		// When & Then
		assertThatThrownBy(() -> fcmRepository.saveToken(null, "value"))
			.isInstanceOf(NullPointerException.class);
	}

	@DisplayName("FCM 토큰이 성공적으로 삭제된다. - Void")
	@Test
	void deleteTokenByMemberId() {
		// When
		fcmRepository.deleteTokenByMemberId(1L);

		// Then
		verify(stringRedisRepository).delete(any(String.class));
	}

	@DisplayName("FCM 토큰 삭제 시, 필요한 값이 NULL 이다. - NullPointerException")
	@Test
	void deleteTokenByMemberId_NullPointerException() {
		// When & Then
		assertThatThrownBy(() -> fcmRepository.deleteTokenByMemberId(null))
			.isInstanceOf(NullPointerException.class);
	}

	@DisplayName("FCM 토큰을 성공적으로 조회된다. - (String) FCM TOKEN")
	@Test
	void findTokenByMemberId() {
		// When
		fcmRepository.findTokenByMemberId(1L);

		// Then
		verify(stringRedisRepository).get(any(String.class));
	}

	@DisplayName("FCM 토큰 조회 시, 필요한 값이 NULL 이다. - NullPointerException")
	@Test
	void findTokenByMemberId_NullPointerException() {
		// When & Then
		assertThatThrownBy(() -> fcmRepository.findTokenByMemberId(null))
			.isInstanceOf(NullPointerException.class);
	}

	@DisplayName("FCM 토큰 존재 여부를 성공적으로 확인한다. - Boolean")
	@Test
	void existsTokenByMemberId() {
		// When
		fcmRepository.existsTokenByMemberId(1L);

		// Then
		verify(stringRedisRepository).hasKey(any(String.class));
	}

	@DisplayName("FCM 토큰 존재 여부 체크 시, 필요한 값이 NULL 이다. - NullPointerException")
	@Test
	void existsTokenByMemberId_NullPointerException() {
		// When & Then
		assertThatThrownBy(() -> fcmRepository.existsTokenByMemberId(null))
			.isInstanceOf(NullPointerException.class);
	}
}
