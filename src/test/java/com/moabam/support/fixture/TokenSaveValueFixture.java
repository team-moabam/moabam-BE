package com.moabam.support.fixture;

import com.moabam.api.dto.auth.TokenSaveValue;

public class TokenSaveValueFixture {

	public static TokenSaveValue tokenSaveValue(String token, String ip) {
		return TokenSaveValue.builder()
			.refreshToken(token)
			.loginIp(ip)
			.build();
	}

	public static TokenSaveValue tokenSaveValue(String token) {
		return TokenSaveValue.builder()
			.refreshToken(token)
			.loginIp("127.0.0.1")
			.build();
	}

	public static TokenSaveValue tokenSaveValue() {
		return tokenSaveValue("token");
	}
}
