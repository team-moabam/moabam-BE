package com.moabam.support.fixture;

import com.moabam.api.dto.AuthorizationTokenResponse;

public class AuthorizationTokenResponseFixture {

	static final String tokenType = "tokenType";
	static final String accessToken = "accessToken";
	static final String idToken = "id";
	static final String expiresin = "exp";
	static final String refreshToken = "ref";
	static final String refreshTokenExpiresIn = "refs";
	static final String scope = "scope";

	public static AuthorizationTokenResponse authorizationTokenResponse() {
		return new AuthorizationTokenResponse(tokenType, accessToken, idToken,
			expiresin, refreshToken, refreshTokenExpiresIn, scope);
	}
}
