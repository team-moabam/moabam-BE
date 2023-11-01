package com.moabam.global.common.repository;

import java.time.Duration;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public abstract class StringRedisRepository {

	private final StringRedisTemplate stringRedisTemplate;

	@Transactional
	public void save(String key, String value, Duration timeout) {
		stringRedisTemplate
			.opsForValue()
			.set(key, value, timeout);
	}

	@Transactional
	public void delete(String key) {
		stringRedisTemplate.delete(key);
	}

	public String get(String key) {
		return stringRedisTemplate
			.opsForValue()
			.get(key);
	}

	public Boolean hasKey(String email) {
		return stringRedisTemplate.hasKey(email);
	}
}
