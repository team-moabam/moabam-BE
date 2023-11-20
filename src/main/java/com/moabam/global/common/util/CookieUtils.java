package com.moabam.global.common.util;

import jakarta.servlet.http.Cookie;

public class CookieUtils {

	public static Cookie tokenCookie(String name, String value, long expireTime) {
		Cookie cookie = new Cookie(name, value);

		return detailCookies(cookie, expireTime);
	}

	public static Cookie typeCookie(String value, long expireTime) {
		Cookie cookie = new Cookie("token_type", value);

		return detailCookies(cookie, expireTime);
	}

	public static Cookie deleteCookie(Cookie cookie) {
		cookie.setMaxAge(0);
		cookie.setPath("/");
		return cookie;
	}

	private static Cookie detailCookies(Cookie cookie, long expireTime) {
		cookie.setSecure(true);
		cookie.setHttpOnly(true);
		cookie.setPath("/");
		cookie.setMaxAge((int)expireTime);
		cookie.setAttribute("SameSite", "Lax");

		return cookie;
	}
}
