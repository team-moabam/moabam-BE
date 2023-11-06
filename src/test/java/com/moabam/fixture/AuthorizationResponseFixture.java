package com.moabam.fixture;

import com.moabam.api.dto.AuthorizationCodeResponse;
import com.moabam.api.dto.AuthorizationTokenInfoResponse;
import com.moabam.api.dto.AuthorizationTokenResponse;

public final class AuthorizationResponseFixture {

	static final String tokenType = "tokenType";
	static final String accessToken = "accessToken";
	static final String idToken = "id";
	static final String expiresin = "exp";
	static final String refreshToken = "ref";
	static final String refreshTokenExpiresIn = "refs";
	static final String scope = "scope";

	public static AuthorizationCodeResponse successCodeResponse() {
		return new AuthorizationCodeResponse("test", null, null, null);
	}

	public static AuthorizationTokenInfoResponse authorizationTokenInfoResponse() {
		return new AuthorizationTokenInfoResponse(1L, "expiresIn", "appId");
	}

	public static AuthorizationTokenResponse authorizationTokenResponse() {
		return new AuthorizationTokenResponse(tokenType, accessToken, idToken,
			expiresin, refreshToken, refreshTokenExpiresIn, scope);
	}
}
