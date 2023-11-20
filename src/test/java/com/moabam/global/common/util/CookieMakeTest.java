package com.moabam.global.common.util;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.servlet.http.Cookie;

@ExtendWith(MockitoExtension.class)
class CookieMakeTest {

	@DisplayName("prod환경에서 cookie 생성 테스트")
	@Test
	void prodUtilsTest() {
		// Given
		Cookie cookie = CookieUtils.tokenCookie("access_token", "value", 10000);

		// When + Then
		assertAll(
			() -> assertThat(cookie.getSecure()).isTrue(),
			() -> assertThat(cookie.getSecure()).isTrue(),
			() -> assertThat(cookie.getPath()).isEqualTo("/"),
			() -> assertThat(cookie.getMaxAge()).isEqualTo(10000),
			() -> assertThat(cookie.getAttribute("SameSite")).isEqualTo("Lax")
		);
	}
}
