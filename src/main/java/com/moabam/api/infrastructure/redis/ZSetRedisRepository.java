package com.moabam.api.infrastructure.redis;

import static java.util.Objects.*;

import java.time.Duration;
import java.util.Set;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ZSetRedisRepository {

	private final RedisTemplate<String, Object> redisTemplate;

	public void addIfAbsent(String key, Object value, double score, int expire) {
		redisTemplate
			.opsForZSet()
			.addIfAbsent(requireNonNull(key), requireNonNull(value), score);
		redisTemplate
			.expire(key, Duration.ofDays(expire));
	}

	public Set<Object> range(String key, long start, long end) {
		return redisTemplate
			.opsForZSet()
			.range(key, start, end);
	}

	public Long rank(String key, Object value) {
		return redisTemplate
			.opsForZSet()
			.rank(key, value);
	}

	public Double score(String key, Object value) {
		return redisTemplate
			.opsForZSet()
			.score(key, value);
	}

	public Long size(String key) {
		return redisTemplate
			.opsForZSet()
			.zCard(key);
	}

	public void add(String key, Object value, double score) {
		redisTemplate
			.opsForZSet()
			.add(requireNonNull(key), requireNonNull(value), score);
	}

	public void changeMember(String key, Object before, Object after) {
		Double score = redisTemplate.opsForZSet().score(key, before);

		if (score == null) {
			return;
		}

		delete(key, before);
		add(key, after, score);
	}

	public void delete(String key, Object value) {
		redisTemplate.opsForZSet().remove(key, value);
	}

	public Set<TypedTuple<Object>> rangeJson(String key, int startIndex, int limitIndex) {
		setSerialize(Object.class);
		Set<ZSetOperations.TypedTuple<Object>> rankings = redisTemplate.opsForZSet()
			.reverseRangeWithScores(key, startIndex, limitIndex);
		setSerialize(String.class);
		return rankings;
	}

	public Long reverseRank(String key, Object myRankingInfo) {
		return redisTemplate.opsForZSet().reverseRank(key, myRankingInfo);
	}

	private void setSerialize(Class classes) {
		redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(classes));
	}
}
