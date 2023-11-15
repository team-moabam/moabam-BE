package com.moabam.api.domain.repository;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.Duration;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.moabam.api.dto.auth.TokenSaveValue;
import com.moabam.api.infrastructure.redis.HashTemplateRepository;
import com.moabam.api.infrastructure.redis.TokenRepository;
import com.moabam.support.fixture.TokenSaveValueFixture;

@ExtendWith(MockitoExtension.class)
public class TokenRepostiroyTest {

	@InjectMocks
	TokenRepository tokenRepository;

	@Mock
	HashTemplateRepository hashTemplateRepository;

	@DisplayName("토큰 저장 성공")
	@Test
	void save_token_suceess() {
		// Given
		willDoNothing().given(hashTemplateRepository).save(any(), any(TokenSaveValue.class), any(Duration.class));

		// When + Then
		Assertions.assertThatNoException()
			.isThrownBy(() -> tokenRepository.saveToken(1L, TokenSaveValueFixture.tokenSaveValue()));
	}

	@Nested
	@DisplayName("토큰 조회")
	class Search {

		@DisplayName("성공")
		@Test
		void token_get_success() {
			// given
			willReturn(TokenSaveValueFixture.tokenSaveValue("token"))
				.given(hashTemplateRepository).get(anyString());

			// when
			TokenSaveValue tokenSaveValue = tokenRepository.getTokenSaveValue(123L);

			// then
			assertAll(
				() -> assertThat(tokenSaveValue).isNotNull(),
				() -> assertThat(tokenSaveValue.refreshToken()).isEqualTo("token")
			);
		}

		@DisplayName("실패")
		@Test
		void token_get_fail() {
			// given
			willReturn(null)
				.given(hashTemplateRepository).get(anyString());

			// when
			TokenSaveValue tokenSaveValue = tokenRepository.getTokenSaveValue(123L);

			// then
			assertThat(tokenSaveValue).isNull();
		}
	}

	@DisplayName("토큰 저장 삭제")
	@Test
	void delete_token_suceess() {
		// When + Then
		Assertions.assertThatNoException()
			.isThrownBy(() -> tokenRepository.delete(1L));
	}
}
