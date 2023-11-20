package com.moabam.api.infrastructure.redis;

import java.time.Duration;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class StringRedisRepository {

	private final RedisTemplate<String, Object> redisTemplate;

	public void save(String key, String value, Duration timeout) {
		redisTemplate
			.opsForValue()
			.set(key, value, timeout);
	}

	public void delete(String key) {
		redisTemplate.delete(key);
	}

	public String get(String key) {
		return (String)redisTemplate
			.opsForValue()
			.get(key);
	}

	public Boolean hasKey(String key) {
		return redisTemplate.hasKey(key);
	}
}
