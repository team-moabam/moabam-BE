package com.moabam.api.infrastructure.redis;

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
import com.moabam.global.config.EmbeddedRedisConfig;
import com.moabam.global.config.RedisConfig;
import com.moabam.global.error.exception.UnauthorizedException;
import com.moabam.global.error.model.ErrorMessage;
import com.moabam.support.fixture.TokenSaveValueFixture;

@SpringBootTest(classes = {RedisConfig.class, EmbeddedRedisConfig.class, HashTemplateRepository.class})
class HashTemplateRepositoryTest {

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
		assertThatThrownBy(() -> hashTemplateRepository.get(key))
			.isInstanceOf(UnauthorizedException.class)
			.hasMessage(ErrorMessage.AUTHENTICATE_FAIL.getMessage());
	}

	@DisplayName("토큰이 null 이어서 예외 발생")
	@Test
	void valid_token_failby_token_is_null() {
		// Given + When + Then
		assertThatThrownBy(() -> hashTemplateRepository.get("0"))
			.isInstanceOf(UnauthorizedException.class)
			.hasMessage(ErrorMessage.AUTHENTICATE_FAIL.getMessage());
	}
}
