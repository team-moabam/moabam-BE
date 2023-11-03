package com.moabam.fixture;

import com.moabam.api.dto.AuthorizationCodeResponse;

public final class AuthorizationCodeResponseFixture {

	public static AuthorizationCodeResponse successCodeResponse() {
		return new AuthorizationCodeResponse("test", null, null, null);
	}
}
