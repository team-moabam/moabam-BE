package com.moabam.api.infrastructure.redis;

import static org.assertj.core.api.Assertions.*;

import java.time.Duration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.moabam.global.config.EmbeddedRedisConfig;

@SpringBootTest(classes = {EmbeddedRedisConfig.class, ValueRedisRepository.class})
class ValueRedisRepositoryTest {

	@Autowired
	ValueRedisRepository valueRedisRepository;

	String key = "key";
	String value = "value";
	String stockKey = "key_INCR";
	Duration duration = Duration.ofMillis(5000);

	@BeforeEach
	void setUp() {
		valueRedisRepository.save(key, value, duration);
	}

	@AfterEach
	void setDown() {
		if (valueRedisRepository.hasKey(key)) {
			valueRedisRepository.delete(key);
		}

		if (valueRedisRepository.hasKey(stockKey)) {
			valueRedisRepository.delete(stockKey);
		}
	}

	@DisplayName("레디스에 문자열 데이터가 성공적으로 저장된다. - Void")
	@Test
	void save_success() {
		// Then
		assertThat(valueRedisRepository.get(key)).isEqualTo(value);
	}

	@DisplayName("레디스의 특정 데이터가 성공적으로 조회된다. - String(Value)")
	@Test
	void get_success() {
		// When
		String actual = valueRedisRepository.get(key);

		// Then
		assertThat(actual).isEqualTo(valueRedisRepository.get(key));
	}

	@DisplayName("레디스의 특정 데이터 존재 여부를 성공적으로 체크한다. - Boolean")
	@Test
	void hasKey_success() {
		// When & Then
		assertThat(valueRedisRepository.hasKey("not found key")).isFalse();
	}

	@DisplayName("레디스의 특정 데이터가 성공적으로 삭제된다. - Void")
	@Test
	void delete_success() {
		// When
		valueRedisRepository.delete(key);

		// Then
		assertThat(valueRedisRepository.hasKey(key)).isFalse();
	}

	@DisplayName("레디스의 특정 데이터의 값이 1 증가한다.")
	@Test
	void increment_success() {
		// When
		Long actual = valueRedisRepository.increment(stockKey);

		// Then
		assertThat(actual).isEqualTo(1L);
	}
}
