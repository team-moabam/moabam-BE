package com.moabam.api.infrastructure.redis;

import java.time.Duration;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class StringRedisRepository {

	private final StringRedisTemplate stringRedisTemplate;

	public void save(String key, String value, Duration timeout) {
		stringRedisTemplate
			.opsForValue()
			.set(key, value, timeout);
	}

	public void delete(String key) {
		stringRedisTemplate.delete(key);
	}

	public String get(String key) {
		return stringRedisTemplate
			.opsForValue()
			.get(key);
	}

	public Boolean hasKey(String key) {
		return stringRedisTemplate.hasKey(key);
	}
}
