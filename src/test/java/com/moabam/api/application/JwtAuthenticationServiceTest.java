package com.moabam.api.application;

import static org.assertj.core.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import java.util.Date;

import org.assertj.core.api.Assertions;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.moabam.global.config.TokenConfig;
import com.moabam.global.error.exception.UnauthorizedException;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationServiceTest {

	String originIss = "PARK";
	String originSecretKey = "testestestestestestestestestesttestestestestestestestestestest";
	long originId = 1L;
	long originAccessExpire = 100000;
	long originRefreshExpire = 150000;

	TokenConfig tokenConfige;
	JwtAuthenticationService jwtAuthenticationService;
	JwtProviderService jwtProviderService;

	@BeforeEach
	void initConfig() {
		tokenConfige = new TokenConfig(originIss, originAccessExpire, originRefreshExpire, originSecretKey);
		jwtProviderService = new JwtProviderService(tokenConfige);
		jwtAuthenticationService = new JwtAuthenticationService(tokenConfige);
	}

	@DisplayName("토큰 인증 시간 만료 테스트")
	@Test
	void token_authentication_time_expire() {
		// Given
		TokenConfig tokenConfig = new TokenConfig(originIss, 0, 0, originSecretKey);
		JwtAuthenticationService jwtAuthenticationService = new JwtAuthenticationService(tokenConfig);
		JwtProviderService jwtProviderService = new JwtProviderService(tokenConfig);
		String token = jwtProviderService.provideAccessToken(originId);

		// When
		assertThatNoException().isThrownBy(() -> {
			boolean result = jwtAuthenticationService.isTokenValid(token);

			// Then
			assertThat(result).isFalse();
		});
	}

	@DisplayName("토큰의 payload 변조되어 인증 실패")
	@Test
	void token_authenticate_failBy_payload() {
		// Given
		String token = jwtProviderService.provideAccessToken(originId);
		String[] parts = token.split("\\.");
		String claims = new String(Base64.getDecoder().decode(parts[1]));

		JSONObject tokenJson = new JSONObject(claims);

		// When
		tokenJson.put("id", "2");

		claims = tokenJson.toString();
		String newToken = String.join(".", parts[0],
			Base64.getEncoder().encodeToString(claims.getBytes()),
			parts[2]);

		// Then
		Assertions.assertThatThrownBy(() -> jwtAuthenticationService.isTokenValid(newToken))
			.isInstanceOf(UnauthorizedException.class);
	}

	@DisplayName("")
	@Test
	void token_authenticate_failBy_key() {
		// Givne
		String fakeKey = "fakefakefakefakefakefakefakefakefakefakefakefake";
		Key key = Keys.hmacShaKeyFor(fakeKey.getBytes(StandardCharsets.UTF_8));

		Date now = new Date();
		String token = Jwts.builder()
			.setHeaderParam("alg", "HS256")
			.setHeaderParam("typ", "JWT")
			.setIssuer(originIss)
			.setIssuedAt(now)
			.setExpiration(new Date(now.getTime() + originAccessExpire))
			.claim("id", 5L)
			.signWith(key, SignatureAlgorithm.HS256)
			.compact();

		// When + Then
		assertThatThrownBy(() -> jwtAuthenticationService.isTokenValid(token))
			.isExactlyInstanceOf(UnauthorizedException.class);
	}
}