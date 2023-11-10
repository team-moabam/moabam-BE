package com.moabam.api.application;

import java.util.Date;

import org.springframework.stereotype.Service;

import com.moabam.global.config.TokenConfig;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtProviderService {

	private final TokenConfig tokenConfig;

	public String provideAccessToken(long id) {
		return generateToken(id, tokenConfig.getAccessExpire());
	}

	public String provideRefreshToken(long id) {
		return generateToken(id, tokenConfig.getRefreshExpire());
	}

	private String generateToken(long id, long expireTime) {
		Date issueDate = new Date();
		Date expireDate = new Date(issueDate.getTime() + expireTime);

		return Jwts.builder()
			.setHeaderParam("alg", "HS256")
			.setHeaderParam("typ", "JWT")
			.setIssuer(tokenConfig.getIss())
			.setIssuedAt(issueDate)
			.setExpiration(expireDate)
			.claim("id", id)
			.signWith(tokenConfig.getKey(), SignatureAlgorithm.HS256)
			.compact();
	}
}
