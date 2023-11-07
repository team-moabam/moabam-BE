package com.moabam.global.common.util;

import jakarta.servlet.http.Cookie;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CookieUtils {

	public static Cookie tokenCookie(String name, String value) {
		Cookie cookie = new Cookie(name, value);
		cookie.setSecure(true);
		cookie.setHttpOnly(true);
		cookie.setPath("/");

		return cookie;
	}
}
