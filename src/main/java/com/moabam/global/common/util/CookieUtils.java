package com.moabam.global.common.util;

import jakarta.servlet.http.Cookie;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CookieUtils {

	public static Cookie tokenCookie(String name, String value, long expireTime, String domain) {
		Cookie cookie = new Cookie(name, value);
		cookie.setSecure(true);
		cookie.setHttpOnly(true);
		cookie.setPath("/");
		cookie.setDomain(domain);
		cookie.setMaxAge((int)expireTime);
		cookie.setAttribute("SameSite", "None");

		return cookie;
	}

	public static Cookie typeCookie(String value, long expireTime, String domain) {
		Cookie cookie = new Cookie("token_type", value);
		cookie.setSecure(true);
		cookie.setHttpOnly(true);
		cookie.setPath("/");
		cookie.setDomain(domain);
		cookie.setMaxAge((int)expireTime);
		cookie.setAttribute("SameSite", "None");

		return cookie;
	}

	public static Cookie deleteCookie(Cookie cookie) {
		cookie.setMaxAge(0);
		cookie.setPath("/");
		return cookie;
	}
}
