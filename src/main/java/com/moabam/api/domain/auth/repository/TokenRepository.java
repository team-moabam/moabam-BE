package com.moabam.api.domain.auth.repository;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.moabam.api.dto.auth.TokenSaveValue;
import com.moabam.api.infrastructure.redis.HashRedisRepository;

@Repository
public class TokenRepository {

	private static final int EXPIRE_DAYS = 14;

	private final HashRedisRepository hashRedisRepository;

	@Autowired
	public TokenRepository(HashRedisRepository hashRedisRepository) {
		this.hashRedisRepository = hashRedisRepository;
	}

	public void saveToken(Long memberId, TokenSaveValue tokenSaveRequest) {
		String tokenKey = parseTokenKey(memberId);

		hashRedisRepository.save(tokenKey, tokenSaveRequest, Duration.ofDays(EXPIRE_DAYS));
	}

	public TokenSaveValue getTokenSaveValue(Long memberId) {
		String tokenKey = parseTokenKey(memberId);
		return (TokenSaveValue)hashRedisRepository.get(tokenKey);
	}

	public void delete(Long memberId) {
		String tokenKey = parseTokenKey(memberId);
		hashRedisRepository.delete(tokenKey);
	}

	private String parseTokenKey(Long memberId) {
		return "auth_" + memberId;
	}
}
