package com.moabam.fixture;

import com.moabam.api.dto.AuthorizationTokenInfoResponse;

public final class AuthorizationTokenInfoResponseFixture {

	public static AuthorizationTokenInfoResponse authorizationTokenInfoResponse() {
		return new AuthorizationTokenInfoResponse(1L, "expiresIn", "appId");
	}
}
