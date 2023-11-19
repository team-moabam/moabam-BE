package com.moabam.global.common.util;

import com.moabam.global.common.util.cookie.CookieDevUtils;
import com.moabam.global.common.util.cookie.CookieProdUtils;
import com.moabam.global.common.util.cookie.CookieUtils;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@ExtendWith(MockitoExtension.class)
class CookieMakeTest {

    CookieUtils cookieDevUtils;
    CookieUtils cookieProdUtils;

    @BeforeEach
    void setUp() {
        cookieDevUtils = new CookieDevUtils();
        cookieProdUtils = new CookieProdUtils();
    }

    @DisplayName("prod환경에서 cookie 생성 테스트")
    @Test
    void prodUtilsTest() {
        // Given
        Cookie cookie = cookieProdUtils.tokenCookie("access_token", "value", 10000);

        // When + Then
        assertAll(
                () -> assertThat(cookie.getSecure()).isTrue(),
                () -> assertThat(cookie.getSecure()).isTrue(),
                () -> assertThat(cookie.getPath()).isEqualTo("/"),
                () -> assertThat(cookie.getMaxAge()).isEqualTo(10000)
        );
    }

    @DisplayName("dev환경에서 cookie 생성 테스트")
    @Test
    void devUtilsTest() {
        // Given
        Cookie cookie = cookieDevUtils.tokenCookie("access_token", "value", 10000);

        // When + Then
        assertAll(
                () -> assertThat(cookie.getSecure()).isTrue(),
                () -> assertThat(cookie.getSecure()).isTrue(),
                () -> assertThat(cookie.getPath()).isEqualTo("/"),
                () -> assertThat(cookie.getMaxAge()).isEqualTo(10000),
                () -> assertThat(cookie.getAttribute("SameSite")).isEqualTo("None")
        );
    }
}
