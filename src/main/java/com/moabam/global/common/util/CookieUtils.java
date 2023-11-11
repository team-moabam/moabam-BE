package com.moabam.global.common.util;

import jakarta.servlet.http.Cookie;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CookieUtils {

	public static Cookie tokenCookie(String name, String value, long expireTime) {
		Cookie cookie = new Cookie(name, value);
		basic(cookie, expireTime);
		secure(cookie);
		return cookie;
	}

	public static Cookie typeCookie(String name, String value, long expireTime) {
		Cookie cookie = new Cookie(name, value);
		basic(cookie, expireTime);
		return cookie;
	}

	private static void secure(Cookie cookie) {
		cookie.setSecure(true);
	}

	private static void basic(Cookie cookie, long expireTime) {
		cookie.setHttpOnly(true);
		cookie.setPath("/");
		cookie.setMaxAge((int)expireTime);
	}
}
