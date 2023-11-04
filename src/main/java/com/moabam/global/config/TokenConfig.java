package com.moabam.global.config;

import java.nio.charset.StandardCharsets;
import java.security.Key;

import org.springframework.boot.context.properties.ConfigurationProperties;

import io.jsonwebtoken.security.Keys;
import lombok.Getter;

@Getter
@ConfigurationProperties(prefix = "token")
public class TokenConfig {

	private String iss;
	private long accessExpire;
	private long refreshExpire;
	private String secretKey;
	private Key key;

	public TokenConfig(String iss, long accessExpire, long refreshExpire, String secretKey) {
		this.iss = iss;
		this.accessExpire = accessExpire;
		this.refreshExpire = refreshExpire;
		this.secretKey = secretKey;
		this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
	}
}
