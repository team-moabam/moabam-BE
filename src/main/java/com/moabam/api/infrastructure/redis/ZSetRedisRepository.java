package com.moabam.api.infrastructure.redis;

import static java.util.Objects.*;

import java.util.Set;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ZSetRedisRepository {

	private final RedisTemplate<String, Object> redisTemplate;

	public void addIfAbsent(String key, Object value, double score) {
		if (redisTemplate.opsForZSet().score(key, value) == null) {
			redisTemplate
				.opsForZSet()
				.add(requireNonNull(key), requireNonNull(value), score);
		}
	}

	public Set<TypedTuple<Object>> popMin(String key, long count) {
		return redisTemplate
			.opsForZSet()
			.popMin(key, count);
	}
}
