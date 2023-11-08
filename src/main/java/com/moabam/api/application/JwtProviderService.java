package com.moabam.api.application;

import java.util.Date;

import org.springframework.stereotype.Service;

import com.moabam.global.config.TokenConfig;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtProviderService {

	private final TokenConfig tokenConfig;

	public String provideAccessToken(Long id) {
		return generateIdToken(id, tokenConfig.getAccessExpire());
	}

	public String provideRefreshToken() {
		return generateCommonInfo(tokenConfig.getRefreshExpire());
	}

	private String generateIdToken(Long id, long expireTime) {
		return commonInfo(expireTime)
			.claim("id", id)
			.compact();
	}

	private String generateCommonInfo(long expireTime) {
		return commonInfo(expireTime).compact();
	}

	private JwtBuilder commonInfo(long expireTime) {
		Date issueDate = new Date();
		Date expireDate = new Date(issueDate.getTime() + expireTime);

		return Jwts.builder()
			.setHeaderParam("alg", "HS256")
			.setHeaderParam("typ", "JWT")
			.setIssuer(tokenConfig.getIss())
			.setIssuedAt(issueDate)
			.setExpiration(expireDate)
			.signWith(tokenConfig.getKey(), SignatureAlgorithm.HS256);
	}
}
