package com.moabam.api.application.auth;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.moabam.api.domain.member.Role;
import com.moabam.global.auth.model.PublicClaim;
import com.moabam.global.config.TokenConfig;
import com.moabam.support.fixture.PublicClaimFixture;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;

class JwtProviderServiceTest {

	String iss = "PARK";
	String secretKey = "testestestestestestestestestesttestestestestestestestestestest";
	String adminKey = "testestestestestestestestestesttestestestestestestestestestest";
	long id = 1L;

	@DisplayName("access 토큰 생성 성공")
	@Test
	void create_access_token_success() throws JSONException {
		// given
		long accessExpire = 10000L;

		TokenConfig tokenConfig = new TokenConfig("PARK", accessExpire, 0L, secretKey, adminKey);
		JwtProviderService jwtProviderService = new JwtProviderService(tokenConfig);
		PublicClaim publicClaim = PublicClaimFixture.publicClaim();

		// when
		String accessToken = jwtProviderService.provideAccessToken(publicClaim);

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

	@DisplayName("토큰 디코딩 실패")
	@Test
	void decoding_token_failBy_url() {
		// given
		String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9"
			+ ".eyJpc3MiOiJtb2Ftb2Ftb2FiYW0iLCJpYXQiOjE3MDEyMzQyNjksImV4c"
			+ "CI6MTcwMTIzNDU2OSwiaWQiOjIsIm5pY2tuYW1lIjoiXHVEODNEXHVEQzNC6rOw64-M7J20Iiwicm9sZSI6IlVTRVIifQ"
			+ ".yVcvshWQ6fsQ0OQ-A5kolDo-8QsLVFCD6dIENKWZH-A";
		String[] parts = token.split("\\.");

		// when + then
		assertThatThrownBy(() -> Base64.getDecoder().decode(parts[1])).isInstanceOf(IllegalArgumentException.class);
	}

	@DisplayName("토큰 디코딩 성공")
	@ParameterizedTest
	@ValueSource(strings = {
		"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9"
			+ ".eyJpc3MiOiJtb2Ftb2Ftb2FiYW0iLCJpYXQiOjE3MDEyMzQyNjksImV4cCI6MTcwMjQ0Mzg2OX0"
			+ ".IrcH_LvBKK1HezgY3PVY-0HQlhP6neEuydH6Mhz4Jgo",
		"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9"
			+ ".eyJpc3MiOiJtb2Ftb2Ftb2FiYW0iLCJpYXQiOjE3MDEyMzQyNjksImV4cCI6MTcwMTIzNDU2OSwiaWQiOjIsIm"
			+ "5pY2tuYW1lIjoiXHVEODNEXHVEQzNC6rOw64-M7J20Iiwicm9sZSI6IlVTRVIifQ"
			+ ".yVcvshWQ6fsQ0OQ-A5kolDo-8QsLVFCD6dIENKWZH-A"
	})
	void decoding_token_success(String token) {
		// given
		String[] parts = token.split("\\.");

		// When + Then
		assertThatNoException().isThrownBy(() -> Decoders.BASE64URL.decode(parts[1]));
	}

	@DisplayName("refresh 토큰 생성 성공")
	@Test
	void create_refresh_token_success() throws JSONException {
		// given
		long refreshExpire = 15000L;

		TokenConfig tokenConfig = new TokenConfig("PARK", 0L, refreshExpire, secretKey, adminKey);
		JwtProviderService jwtProviderService = new JwtProviderService(tokenConfig);

		// when
		String refreshToken = jwtProviderService.provideRefreshToken(Role.USER);

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

	@DisplayName("access 토큰 만료시간에 따른 생성 실패")
	@Test
	void create_access_token_fail() {
		// given
		long accessExpire = -1L;

		TokenConfig tokenConfig = new TokenConfig("PARK", accessExpire, 0L, secretKey, adminKey);
		JwtProviderService jwtProviderService = new JwtProviderService(tokenConfig);
		PublicClaim publicClaim = PublicClaimFixture.publicClaim();

		// when
		String accessToken = jwtProviderService.provideAccessToken(publicClaim);

		// then
		assertThatThrownBy(() -> Jwts.parserBuilder()
			.setSigningKey(tokenConfig.getKey())
			.build()
			.parseClaimsJwt(accessToken)
		).isInstanceOf(ExpiredJwtException.class);
	}

	@DisplayName("refresh 토큰 만료시간에 따른 생성 실패")
	@Test
	void create_token_fail() {
		// given
		long refreshExpire = -1L;

		TokenConfig tokenConfig = new TokenConfig("PARK", 0L, refreshExpire, secretKey, adminKey);
		JwtProviderService jwtProviderService = new JwtProviderService(tokenConfig);
		PublicClaim publicClaim = PublicClaimFixture.publicClaim();

		// when
		String accessToken = jwtProviderService.provideAccessToken(publicClaim);

		// then
		assertThatThrownBy(() -> Jwts.parserBuilder()
			.setSigningKey(tokenConfig.getKey())
			.build()
			.parseClaimsJwt(accessToken)
		).isExactlyInstanceOf(ExpiredJwtException.class);
	}
}
