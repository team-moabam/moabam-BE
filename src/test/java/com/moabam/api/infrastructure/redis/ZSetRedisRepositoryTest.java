package com.moabam.api.infrastructure.redis;

import static org.assertj.core.api.Assertions.*;

import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;

import com.moabam.global.config.EmbeddedRedisConfig;

@SpringBootTest(classes = {EmbeddedRedisConfig.class, ZSetRedisRepository.class, StringRedisRepository.class})
class ZSetRedisRepositoryTest {

	@Autowired
	ZSetRedisRepository zSetRedisRepository;

	@Autowired
	StringRedisRepository stringRedisRepository;

	@Autowired
	RedisTemplate<String, Object> redisTemplate;

	String key = "key";
	Long value = 1L;

	@AfterEach
	void afterEach() {
		if (stringRedisRepository.hasKey(key)) {
			stringRedisRepository.delete(key);
		}
	}

	@DisplayName("레디스의 SortedSet 데이터가 성공적으로 저장된다. - Void")
	@Test
	void addIfAbsent_success() {
		// When
		zSetRedisRepository.addIfAbsent(key, value, 1);

		// Then
		assertThat(stringRedisRepository.hasKey(key)).isTrue();
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

	@DisplayName("저장된 데이터와 동일한 갯수만큼의 반환과 삭제를 성공적으로 시도한다. - Set<TypedTuple<Object>>")
	@Test
	void popMin_same_success() {
		// Given
		zSetRedisRepository.addIfAbsent(key, value + 1, 1);
		zSetRedisRepository.addIfAbsent(key, value + 2, 2);
		zSetRedisRepository.addIfAbsent(key, value + 3, 3);

		// When
		Set<TypedTuple<Object>> actual = zSetRedisRepository.popMin(key, 3);

		// Then
		assertThat(actual).hasSize(3);
		assertThat(stringRedisRepository.hasKey(key)).isFalse();
	}

	@DisplayName("저장된 데이터보다 많은 갯수만큼의 반환과 삭제를 성공적으로 시도한다. - Set<TypedTuple<Object>>")
	@Test
	void popMin_more_success() {
		// Given
		zSetRedisRepository.addIfAbsent(key, value + 1, 1);
		zSetRedisRepository.addIfAbsent(key, value + 2, 2);

		// When
		Set<TypedTuple<Object>> actual = zSetRedisRepository.popMin(key, 3);

		// Then
		assertThat(actual).hasSize(2);
	}

	@DisplayName("저장된 데이터보다 더 적은 갯수만큼의 반환과 삭제를 성공적으로 시도한다. - Set<TypedTuple<Object>>")
	@Test
	void popMin_less_success() {
		// Given
		zSetRedisRepository.addIfAbsent(key, value + 1, 1);
		zSetRedisRepository.addIfAbsent(key, value + 2, 2);
		zSetRedisRepository.addIfAbsent(key, value + 3, 3);
		zSetRedisRepository.addIfAbsent(key, value + 4, 4);
		zSetRedisRepository.addIfAbsent(key, value + 5, 5);

		// When
		Set<TypedTuple<Object>> actual = zSetRedisRepository.popMin(key, 3);

		// Then
		assertThat(actual).hasSize(3);
		assertThat(stringRedisRepository.hasKey(key)).isTrue();
	}
}
