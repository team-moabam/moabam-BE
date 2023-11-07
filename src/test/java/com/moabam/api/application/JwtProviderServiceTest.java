package com.moabam.api.application;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.moabam.global.config.TokenConfig;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;

class JwtProviderServiceTest {

	String iss = "PARK";
	String secretKey = "testestestestestestestestestesttestestestestestestestestestest";
	long id = 1L;

	@DisplayName("access 토큰 생성 성공")
	@Test
	void create_access_token_success() throws JSONException {
		// given
		long accessExpire = 10000L;

		TokenConfig tokenConfig = new TokenConfig("PARK", accessExpire, 0L, secretKey);
		JwtProviderService jwtProviderService = new JwtProviderService(tokenConfig);

		// when
		String accessToken = jwtProviderService.provideAccessToken(id);

		String[] parts = accessToken.split("\\.");
		String headers = new String(Base64.getDecoder().decode(parts[0]));
		String claims = new String(Base64.getDecoder().decode(parts[1]));

		JSONObject headersJson = new JSONObject(headers);
		JSONObject claimsJson = new JSONObject(claims);

		// then
		assertAll(
			() -> assertThat(headersJson.get("alg")).isEqualTo("HS256"),
			() -> assertThat(headersJson.get("typ")).isEqualTo("JWT"),
			() -> assertThat(claimsJson.get("iss")).isEqualTo(iss)
		);

		Long iat = Long.valueOf(claimsJson.get("iat").toString());
		Long exp = Long.valueOf(claimsJson.get("exp").toString());
		assertThat(iat).isLessThan(exp);
	}

	@DisplayName("refresh 토큰 생성 성공")
	@Test
	void create_refresh_token_success() throws JSONException {
		// given
		long refreshExpire = 15000L;

		TokenConfig tokenConfig = new TokenConfig("PARK", 0L, refreshExpire, secretKey);
		JwtProviderService jwtProviderService = new JwtProviderService(tokenConfig);

		// when
		String refreshToken = jwtProviderService.provideRefreshToken(id);

		String[] parts = refreshToken.split("\\.");
		String headers = new String(Base64.getDecoder().decode(parts[0]));
		String claims = new String(Base64.getDecoder().decode(parts[1]));

		JSONObject headersJson = new JSONObject(headers);
		JSONObject claimsJson = new JSONObject(claims);

		// then
		assertAll(
			() -> assertThat(headersJson.get("alg")).isEqualTo("HS256"),
			() -> assertThat(headersJson.get("typ")).isEqualTo("JWT"),
			() -> assertThat(claimsJson.get("iss")).isEqualTo(iss)
		);

		Long iat = Long.valueOf(claimsJson.get("iat").toString());
		Long exp = Long.valueOf(claimsJson.get("exp").toString());
		assertThat(iat).isLessThan(exp);
	}

	@DisplayName("access 토큰 생성 실패")
	@Test
	void create_access_token_fail() {
		// given
		long accessExpire = -1L;

		TokenConfig tokenConfig = new TokenConfig("PARK", accessExpire, 0L, secretKey);
		JwtProviderService jwtProviderService = new JwtProviderService(tokenConfig);

		// when
		String accessToken = jwtProviderService.provideAccessToken(id);

		// then
		assertThatThrownBy(() -> Jwts.parserBuilder()
			.setSigningKey(tokenConfig.getKey())
			.build()
			.parseClaimsJwt(accessToken)
		).isInstanceOf(ExpiredJwtException.class);
	}

	@DisplayName("refresh 토큰 생성 실패")
	@Test
	void create_token_fail() {
		// given
		long refreshExpire = 15000L;

		TokenConfig tokenConfig = new TokenConfig("PARK", 0L, refreshExpire, secretKey);
		JwtProviderService jwtProviderService = new JwtProviderService(tokenConfig);

		// when
		String accessToken = jwtProviderService.provideAccessToken(id);

		// then
		assertThatThrownBy(() -> Jwts.parserBuilder()
			.setSigningKey(tokenConfig.getKey())
			.build()
			.parseClaimsJwt(accessToken)
		).isExactlyInstanceOf(ExpiredJwtException.class);
	}
}
