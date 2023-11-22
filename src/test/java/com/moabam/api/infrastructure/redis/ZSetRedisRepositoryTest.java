package com.moabam.api.infrastructure.redis;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import com.moabam.global.config.EmbeddedRedisConfig;

@SpringBootTest(classes = {EmbeddedRedisConfig.class, ZSetRedisRepository.class})
class ZSetRedisRepositoryTest {

	@Autowired
	private ZSetRedisRepository zSetRedisRepository;

	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	String key = "key";
	Long value = 1L;

	@AfterEach
	void afterEach() {
		if (zSetRedisRepository.hasKey(key)) {
			zSetRedisRepository.delete(key);
		}
	}

	@DisplayName("레디스의 SortedSet 데이터가 성공적으로 저장된다. - Void")
	@Test
	void setRedisRepository_addIfAbsent() {
		// When
		zSetRedisRepository.addIfAbsent(key, value, 1);

		// Then
		assertThat(zSetRedisRepository.hasKey(key)).isTrue();
	}

	@DisplayName("이미 존재하는 값을 한 번 더 저장을 시도한다. - Void")
	@Test
	void setRedisRepository_addIfAbsent_not_update() {
		// When
		zSetRedisRepository.addIfAbsent(key, value, 1);
		zSetRedisRepository.addIfAbsent(key, value, 5);

		// Then
		assertThat(redisTemplate.opsForZSet().score(key, value)).isEqualTo(1);
	}

	@DisplayName("레디스의 특정 키의 사이즈가 성공적으로 반환된다. - int")
	@Test
	void setRedisRepository_size() {
		// Given
		zSetRedisRepository.addIfAbsent(key, value, 1);

		// When
		long actual = zSetRedisRepository.size(key);

		// Then
		assertThat(actual).isEqualTo(1);
	}

	@DisplayName("레디스의 특정 데이터가 성공적으로 삭제된다. - Void")
	@Test
	void setRedisRepository_delete() {
		// Given
		zSetRedisRepository.addIfAbsent(key, value, 1);

		// When
		zSetRedisRepository.delete(key);

		// Then
		assertThat(zSetRedisRepository.hasKey(key)).isFalse();
	}

	@DisplayName("레디스의 특정 데이터 존재 여부를 성공적으로 체크한다. - Boolean")
	@Test
	void setRedisRepository_hasKey() {
		// When & Then
		assertThat(zSetRedisRepository.hasKey("not found key")).isFalse();
	}
}
