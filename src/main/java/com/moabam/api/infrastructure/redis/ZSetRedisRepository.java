package com.moabam.api.infrastructure.redis;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ZSetRedisRepository {

	private final RedisTemplate<String, Object> redisTemplate;

	public void addIfAbsent(String key, String value, double score) {
		redisTemplate
			.opsForZSet()
			.addIfAbsent(key, value, score);
	}

	public Long size(String key) {
		return redisTemplate
			.opsForZSet()
			.size(key);
	}

	public Boolean hasKey(String key) {
		return redisTemplate.hasKey(key);
	}

	public void delete(String key) {
		redisTemplate.delete(key);
	}
}
