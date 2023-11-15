package com.moabam.api.infrastructure.redis;

import java.time.Duration;
import java.util.Date;
import java.util.Map;

import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.hash.Jackson2HashMapper;
import org.springframework.stereotype.Repository;

@Repository
public class HashTemplateRepository {

	private final RedisTemplate<String, Object> redisTemplate;
	private final HashOperations<String, String, Object> hashOperations;
	private final Jackson2HashMapper hashMapper;

	public HashTemplateRepository(RedisTemplate<String, Object> redisTemplate) {
		this.redisTemplate = redisTemplate;
		hashOperations = redisTemplate.opsForHash();
		hashMapper = new Jackson2HashMapper(false);
	}

	public void save(String key, Object value, Duration timeout) {
		hashOperations.putAll(key, hashMapper.toHash(value));
		redisTemplate.expire(key, timeout);
	}

	public void delete(String key) {
		redisTemplate.expireAt(key, new Date());
	}

	public Object get(String key) {
		Map<String, Object> memberToken = hashOperations.entries(key);

		if (memberToken.size() == 0) {
			return null;
		}

		return hashMapper.fromHash(memberToken);
	}
}
