package com.moabam.api.infrastructure.redis;

import static java.util.Objects.*;

import java.time.Duration;
import java.util.Set;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ZSetRedisRepository {

	private final RedisTemplate<String, Object> redisTemplate;

	public void addIfAbsent(String key, Object value, double score, int expire) {
		if (redisTemplate.opsForZSet().score(key, value) == null) {
			redisTemplate
				.opsForZSet()
				.add(requireNonNull(key), requireNonNull(value), score);
			redisTemplate.expire(key, Duration.ofDays(expire));
		}
	}

	public Set<Object> range(String key, long start, long end) {
		return redisTemplate
			.opsForZSet()
			.range(key, start, end);
	}

	public Long size(String key) {
		return redisTemplate.opsForZSet().size(key);
	}
}
