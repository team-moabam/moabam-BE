package com.moabam.api.infrastructure.redis;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ZSetRedisRepository {

	private final RedisTemplate<String, String> redisTemplate;

	public void addIfAbsent(String key, String value, double score) {
		redisTemplate
			.opsForZSet()
			.addIfAbsent(key, value, score);
	}

	public Set<String> range(String key, long startRank, long endRank) {
		return redisTemplate
			.opsForZSet()
			.range(key, startRank, endRank);
	}

	public Set<String> popMin(String key, long count) {
		return Objects.requireNonNull(redisTemplate
				.opsForZSet()
				.popMin(key, count))
			.stream()
			.map(ZSetOperations.TypedTuple::getValue)
			.collect(Collectors.toSet());
	}

	public void remove(String key, String value) {
		redisTemplate
			.opsForZSet()
			.remove(key, value);
	}

	public Long size(String key) {
		return redisTemplate
			.opsForZSet()
			.size(key);
	}
}
