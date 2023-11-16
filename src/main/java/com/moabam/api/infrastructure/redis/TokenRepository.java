package com.moabam.api.infrastructure.redis;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.moabam.api.dto.auth.TokenSaveValue;

@Repository
public class TokenRepository {

	private static final int EXPIRE_DAYS = 14;

	private final HashTemplateRepository hashTemplateRepository;

	@Autowired
	public TokenRepository(HashTemplateRepository hashTemplateRepository) {
		this.hashTemplateRepository = hashTemplateRepository;
	}

	public void saveToken(Long memberId, TokenSaveValue tokenSaveRequest) {
		String tokenKey = parseTokenKey(memberId);

		hashTemplateRepository.save(tokenKey, tokenSaveRequest, Duration.ofDays(EXPIRE_DAYS));
	}

	public TokenSaveValue getTokenSaveValue(Long memberId) {
		String tokenKey = parseTokenKey(memberId);
		return (TokenSaveValue)hashTemplateRepository.get(tokenKey);
	}

	public void delete(Long memberId) {
		String tokenKey = parseTokenKey(memberId);
		hashTemplateRepository.delete(tokenKey);
	}

	private String parseTokenKey(Long memberId) {
		return "auth_" + memberId;
	}
}
