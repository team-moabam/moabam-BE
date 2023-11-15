package com.moabam.global.common.repository;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.moabam.api.dto.auth.TokenSaveValue;
import com.moabam.api.infrastructure.redis.HashTemplateRepository;
import com.moabam.global.config.EmbeddedRedisConfig;
import com.moabam.global.config.RedisConfig;
import com.moabam.support.fixture.TokenSaveValueFixture;

@SpringBootTest(classes = {RedisConfig.class, EmbeddedRedisConfig.class, HashTemplateRepository.class})
public class HashTemplateRepositoryTest {

	@Autowired
	private HashTemplateRepository hashTemplateRepository;

	String key = "auth_123";
	String token = "token";
	String ip = "ip";
	TokenSaveValue tokenSaveValue = TokenSaveValueFixture.tokenSaveValue(token, ip);
	Duration duration = Duration.ofMillis(5000);

	@BeforeEach
	void setUp() {
		hashTemplateRepository.save(key, (Object)tokenSaveValue, duration);
	}

	@AfterEach
	void delete() {
		hashTemplateRepository.delete(key);
	}

	@DisplayName("레디스에 hash 저장 성공")
	@Test
	void hashTemplate_repository_save_success() {
		// Given + When
		TokenSaveValue object = (TokenSaveValue)hashTemplateRepository.get(key);

		// Then
		assertAll(
			() -> assertThat(object.refreshToken()).isEqualTo(token),
			() -> assertThat(object.loginIp()).isEqualTo(ip)
		);
	}

	@DisplayName("삭제 성공 테스트")
	@Test
	void delete_and_get_null() {
		// Given
		hashTemplateRepository.delete(key);

		// When + Then
		assertThat(hashTemplateRepository.get(key)).isNull();
	}
}
