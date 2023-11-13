package com.moabam.support.fixture;

import com.moabam.api.application.auth.JwtProviderService;
import com.moabam.global.config.TokenConfig;

public class JwtProviderFixture {

	public static final String originIss = "PARK";
	public static final String originSecretKey = "testestestestestestestestestesttestestestestestestestestestest";
	public static final long originId = 1L;
	public static final long originAccessExpire = 100000;
	public static final long originRefreshExpire = 150000;

	public static JwtProviderService jwtProviderService() {
		TokenConfig tokenConfig = new TokenConfig(originIss, originAccessExpire, originRefreshExpire, originSecretKey);

		return new JwtProviderService(tokenConfig);
	}
}
