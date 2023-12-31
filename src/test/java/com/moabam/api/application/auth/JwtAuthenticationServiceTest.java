package com.moabam.api.application.auth;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

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

import com.moabam.api.domain.member.Role;
import com.moabam.global.auth.model.PublicClaim;
import com.moabam.global.config.TokenConfig;
import com.moabam.global.error.exception.UnauthorizedException;
import com.moabam.support.fixture.PublicClaimFixture;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationServiceTest {

	String originIss = "PARK";
	String originSecretKey = "testestestestestestestestestesttestestestestestestestestestest";
	String adminKey = "testestestestestestestestestesttestestestestestestestestestest";
	long originId = 1L;
	long originAccessExpire = 100000;
	long originRefreshExpire = 150000;

	TokenConfig tokenConfig;
	JwtAuthenticationService jwtAuthenticationService;
	JwtProviderService jwtProviderService;

	@BeforeEach
	void initConfig() {
		tokenConfig = new TokenConfig(originIss, originAccessExpire, originRefreshExpire, originSecretKey, adminKey);
		jwtProviderService = new JwtProviderService(tokenConfig);
		jwtAuthenticationService = new JwtAuthenticationService(tokenConfig);
	}

	@DisplayName("토큰 인증 성공 테스트")
	@Test
	void token_authentication_success() {
		// given
		String token = jwtProviderService.provideAccessToken(PublicClaimFixture.publicClaim());

		// when, then
		assertThatNoException().isThrownBy(() ->
			jwtAuthenticationService.isTokenExpire(token, Role.USER));
	}

	@DisplayName("토큰 인증 시간 만료 테스트")
	@Test
	void token_authentication_time_expire() {
		// Given
		PublicClaim publicClaim = PublicClaimFixture.publicClaim();
		TokenConfig tokenConfig = new TokenConfig(originIss, 0, 0, originSecretKey, adminKey);
		JwtAuthenticationService jwtAuthenticationService = new JwtAuthenticationService(tokenConfig);
		JwtProviderService jwtProviderService = new JwtProviderService(tokenConfig);
		String token = jwtProviderService.provideAccessToken(publicClaim);

		// When
		assertThatNoException().isThrownBy(() -> {
			boolean result = jwtAuthenticationService.isTokenExpire(token, Role.USER);

			// Then
			assertThat(result).isTrue();
		});
	}

	@DisplayName("토큰의 payload 변조되어 인증 실패")
	@Test
	void token_authenticate_failBy_payload() {
		// Given
		PublicClaim publicClaim = PublicClaimFixture.publicClaim();

		String token = jwtProviderService.provideAccessToken(publicClaim);
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
		Assertions.assertThatThrownBy(() -> jwtAuthenticationService.isTokenExpire(newToken, Role.USER))
			.isInstanceOf(UnauthorizedException.class);
	}

	@DisplayName("토큰 위조 값 검증 테스트")
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
		assertThatThrownBy(() -> jwtAuthenticationService.isTokenExpire(token, Role.USER))
			.isExactlyInstanceOf(UnauthorizedException.class);
	}

	@DisplayName("토큰을 PublicClaim으로 변환 성공")
	@Test
	void token_parse_to_public_claim() {
		// given
		PublicClaim publicClaim = PublicClaimFixture.publicClaim();
		String token = jwtProviderService.provideAccessToken(publicClaim);

		// when
		PublicClaim parsedClaim = jwtAuthenticationService.parseClaim(token);

		// then
		assertAll(
			() -> assertThat(publicClaim.id()).isEqualTo(parsedClaim.id()),
			() -> assertThat(publicClaim.nickname()).isEqualTo(parsedClaim.nickname()),
			() -> assertThat(publicClaim.role()).isEqualTo(parsedClaim.role())
		);
	}
}
