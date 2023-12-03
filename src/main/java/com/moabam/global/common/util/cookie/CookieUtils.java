package com.moabam.global.common.util.cookie;

import jakarta.servlet.http.Cookie;

public abstract class CookieUtils {

	public Cookie tokenCookie(String name, String value, long expireTime) {
		return detailCookies(name, value, expireTime);
	}

	public Cookie typeCookie(String value, long expireTime) {
		return detailCookies("token_type", value, expireTime);
	}

	public Cookie deleteCookie(Cookie cookie) {
		cookie.setMaxAge(0);
		cookie.setPath("/");
		return cookie;
	}

	protected abstract Cookie detailCookies(String name, String value, long expireTime);
}
