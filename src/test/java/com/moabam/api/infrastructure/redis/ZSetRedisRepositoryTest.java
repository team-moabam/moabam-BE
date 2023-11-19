package com.moabam.api.infrastructure.redis;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.moabam.global.config.EmbeddedRedisConfig;
import com.moabam.global.config.RedisConfig;

@SpringBootTest(classes = {RedisConfig.class, EmbeddedRedisConfig.class, ZSetRedisRepository.class})
class ZSetRedisRepositoryTest {

	@Autowired
	private ZSetRedisRepository zSetRedisRepository;

	private String key = "key";
	private String value = "value";
	private int size = 3;

	@BeforeEach
	void setUp() {
		for (int i = 0; i < size; i++) {
			zSetRedisRepository.addIfAbsent(key, value + i, i);
		}
	}

	@AfterEach
	void setDown() {
		zSetRedisRepository.delete(key);
	}

	@DisplayName("레디스의 SortedSet 데이터가 성공적으로 저장된다. - Void")
	@Test
	void zsetRedisRepository_addIfAbsent() {
		// Then
		assertThat(zSetRedisRepository.size(key)).isEqualTo(size);
	}

	@DisplayName("레디스의 특정 키의 사이즈가 성공적으로 반환된다. - int")
	@Test
	void size() {
		// Then
		assertThat(zSetRedisRepository.size(key)).isEqualTo(size);
	}

	@DisplayName("레디스의 특정 데이터가 성공적으로 삭제된다. - Void")
	@Test
	void stringRedisRepository_delete() {
		// When
		zSetRedisRepository.delete(key);

		// Then
		assertThat(zSetRedisRepository.hasKey(key)).isFalse();
	}

	@DisplayName("레디스의 특정 데이터 존재 여부를 성공적으로 체크한다. - Boolean")
	@Test
	void stringRedisRepository_hasKey() {
		// When & Then
		assertThat(zSetRedisRepository.hasKey("not found key")).isFalse();
	}
}
