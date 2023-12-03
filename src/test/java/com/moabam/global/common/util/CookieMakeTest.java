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

	String domain = "test";

	@DisplayName("prod환경에서 cookie 생성 테스트")
	@Test
	void create_test() {
		// Given
		Cookie cookie = CookieUtils.tokenCookie("access_token", "value", 10000, domain);

		// When + Then
		assertAll(
			() -> assertThat(cookie.getSecure()).isTrue(),
			() -> assertThat(cookie.getSecure()).isTrue(),
			() -> assertThat(cookie.getPath()).isEqualTo("/"),
			() -> assertThat(cookie.getMaxAge()).isEqualTo(10000),
			() -> assertThat(cookie.getAttribute("SameSite")).isEqualTo("None")
		);
	}

	@DisplayName("")
	@Test
	void delete_test() {
		// given
		Cookie cookie = CookieUtils.tokenCookie("access_token", "value", 10000, domain);

		// when
		Cookie deletedCookie = CookieUtils.deleteCookie(cookie);

		// then
		assertAll(
			() -> assertThat(deletedCookie.getMaxAge()).isZero(),
			() -> assertThat(deletedCookie.getPath()).isEqualTo("/")
		);
	}

	@DisplayName("")
	@Test
	void typeCookie_create_test() {
		// Given + When
		Cookie cookie = CookieUtils.typeCookie("Bearer", 10000, domain);

		// then
		assertThat(cookie.getName()).isEqualTo("token_type");
	}
}
