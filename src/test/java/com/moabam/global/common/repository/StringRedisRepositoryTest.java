package com.moabam.global.common.repository;

import static org.mockito.BDDMockito.*;

import java.time.Duration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class StringRedisRepositoryTest {

	@InjectMocks
	private StringRedisRepository stringRedisRepository;

	@Mock
	private StringRedisTemplate stringRedisTemplate;

	@Spy
	private ValueOperations<String, String> valueOperations;

	@DisplayName("레디스에 문자열 데이터가 성공적으로 저장될 때, - Void")
	@Test
	void string_redis_repository_save() {
		// Given
		given(stringRedisTemplate.opsForValue()).willReturn(valueOperations);

		// When
		stringRedisRepository.save("key1", "value", Duration.ofHours(1));

		// Then
		verify(stringRedisTemplate.opsForValue()).set(any(String.class), any(String.class), any(Duration.class));
	}

	@DisplayName("레디스의 특정 데이터가 성공적으로 삭제될 때, - Void")
	@Test
	void string_redis_repository_delete() {
		// When
		stringRedisRepository.delete("key2");

		// Then
		verify(stringRedisTemplate).delete(any(String.class));
	}

	@DisplayName("레디스의 특정 데이터가 성공적으로 조회될 때, - String(Value)")
	@Test
	void string_redis_repository_get() {
		// Given
		given(stringRedisTemplate.opsForValue()).willReturn(valueOperations);

		// When
		stringRedisRepository.get("key3");

		// Then
		verify(stringRedisTemplate.opsForValue()).get(any(String.class));
	}

	@DisplayName("레디스의 특정 데이터 존재 여부를 성공적으로 체크할 때, - Boolean")
	@Test
	void string_redis_repository_hasKey() {
		// When
		stringRedisRepository.hasKey("not found key");

		// Then
		verify(stringRedisTemplate).hasKey(any(String.class));
	}
}
