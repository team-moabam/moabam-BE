package com.moabam.api.application.auth;

import java.security.Key;
import java.util.Date;

import org.springframework.stereotype.Service;

import com.moabam.api.domain.member.Role;
import com.moabam.global.auth.model.PublicClaim;
import com.moabam.global.config.TokenConfig;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtProviderService {

	private final TokenConfig tokenConfig;

	public String provideAccessToken(PublicClaim publicClaim) {
		return generateIdToken(publicClaim, tokenConfig.getAccessExpire());
	}

	public String provideRefreshToken(Role role) {
		return generateCommonInfo(tokenConfig.getRefreshExpire(), role);
	}

	private String generateIdToken(PublicClaim publicClaim, long expireTime) {
		return commonInfo(expireTime, publicClaim.role())
			.claim("id", publicClaim.id())
			.claim("nickname", publicClaim.nickname())
			.claim("role", publicClaim.role())
			.compact();
	}

	private String generateCommonInfo(long expireTime, Role role) {
		return commonInfo(expireTime, role).compact();
	}

	private JwtBuilder commonInfo(long expireTime, Role role) {
		Date issueDate = new Date();
		Date expireDate = new Date(issueDate.getTime() + expireTime);

		return Jwts.builder()
			.setHeaderParam("alg", "HS256")
			.setHeaderParam("typ", "JWT")
			.setIssuer(tokenConfig.getIss())
			.setIssuedAt(issueDate)
			.setExpiration(expireDate)
			.signWith(getSecretKey(role), SignatureAlgorithm.HS256);
	}

	private Key getSecretKey(Role role) {
		if (role.equals(Role.ADMIN)) {
			return tokenConfig.getAdminKey();
		}

		return tokenConfig.getKey();
	}
}
