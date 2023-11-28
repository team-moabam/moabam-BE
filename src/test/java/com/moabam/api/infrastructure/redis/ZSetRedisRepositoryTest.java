package com.moabam.api.infrastructure.redis;

import static org.assertj.core.api.Assertions.*;

import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import com.moabam.global.config.EmbeddedRedisConfig;

@SpringBootTest(classes = {EmbeddedRedisConfig.class, ZSetRedisRepository.class, ValueRedisRepository.class})
class ZSetRedisRepositoryTest {

	@Autowired
	ZSetRedisRepository zSetRedisRepository;

	@Autowired
	ValueRedisRepository valueRedisRepository;

	@Autowired
	RedisTemplate<String, Object> redisTemplate;

	String key = "key";
	Long value = 1L;
	int expireDays = 2;

	@AfterEach
	void afterEach() {
		if (valueRedisRepository.hasKey(key)) {
			valueRedisRepository.delete(key);
		}
	}

	@Disabled
	@DisplayName("레디스의 SortedSet 데이터가 성공적으로 저장된다. - Void")
	@Test
	void addIfAbsent_success() {
		// When
		zSetRedisRepository.addIfAbsent(key, value, 1, expireDays);

		// Then
		assertThat(valueRedisRepository.hasKey(key)).isTrue();
	}

	@Disabled
	@DisplayName("이미 존재하는 값을 한 번 더 저장을 시도한다. - Void")
	@Test
	void setRedisRepository_addIfAbsent_not_update() {
		// When
		zSetRedisRepository.addIfAbsent(key, value, 1, expireDays);
		zSetRedisRepository.addIfAbsent(key, value, 5, expireDays);

		// Then
		assertThat(redisTemplate.opsForZSet().score(key, value)).isEqualTo(1);
	}

	@DisplayName("저장된 데이터와 동일한 갯수만큼 조회한다. - Set<Object>")
	@Test
	void range_same_success() {
		// Given
		zSetRedisRepository.addIfAbsent(key, value + 1, 1, expireDays);
		zSetRedisRepository.addIfAbsent(key, value + 2, 2, expireDays);
		zSetRedisRepository.addIfAbsent(key, value + 3, 3, expireDays);

		// When
		Set<Object> actual = zSetRedisRepository.range(key, 0, 3);

		// Then
		assertThat(actual).hasSize(3);
	}

	@DisplayName("저장된 데이터보다 많은 갯수만큼 조회한다. - Set<Object>")
	@Test
	void range_more_success() {
		// Given
		zSetRedisRepository.addIfAbsent(key, value + 1, 1, expireDays);
		zSetRedisRepository.addIfAbsent(key, value + 2, 2, expireDays);

		// When
		Set<Object> actual = zSetRedisRepository.range(key, 0, 3);

		// Then
		assertThat(actual).hasSize(2);
	}

	@DisplayName("저장된 데이터보다 더 적은 갯수만큼 조회한다. - Set<Object>")
	@Test
	void range_less_success() {
		// Given
		zSetRedisRepository.addIfAbsent(key, value + 1, 1, expireDays);
		zSetRedisRepository.addIfAbsent(key, value + 2, 2, expireDays);
		zSetRedisRepository.addIfAbsent(key, value + 3, 3, expireDays);
		zSetRedisRepository.addIfAbsent(key, value + 4, 4, expireDays);
		zSetRedisRepository.addIfAbsent(key, value + 5, 5, expireDays);

		// When
		Set<Object> actual = zSetRedisRepository.range(key, 0, 3);

		// Then
		assertThat(actual).hasSize(3);
	}
}
