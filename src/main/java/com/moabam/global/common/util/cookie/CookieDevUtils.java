package com.moabam.global.common.util.cookie;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.Cookie;

@Component
@Profile({"dev", "local", "test"})
public class CookieDevUtils extends CookieUtils {

	protected Cookie detailCookies(String name, String value, long expireTime) {
		Cookie cookie = new Cookie(name, value);
		cookie.setSecure(true);
		cookie.setHttpOnly(true);
		cookie.setPath("/");
		cookie.setMaxAge((int)expireTime);
		cookie.setAttribute("SameSite", "None");

		return cookie;
	}
}
