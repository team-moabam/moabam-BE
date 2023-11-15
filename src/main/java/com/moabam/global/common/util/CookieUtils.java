package com.moabam.global.common.util;

import jakarta.servlet.http.Cookie;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CookieUtils {

	public static Cookie tokenCookie(String name, String value, long expireTime) {
		return basic(name, value, expireTime);
	}

	public static Cookie typeCookie(String value, long expireTime) {
		return basic("token_type", value, expireTime);
	}

	public static Cookie deleteCookie(Cookie cookie) {
		cookie.setMaxAge(0);
		cookie.setPath("/");
		return cookie;
	}

	private static Cookie basic(String name, String value, long expireTime) {
		Cookie cookie = new Cookie(name, value);
		cookie.setSecure(true);
		cookie.setHttpOnly(true);
		cookie.setPath("/");
		cookie.setMaxAge((int)expireTime);

		return cookie;
	}
}
