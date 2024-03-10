package com.moabam.global.config;

import static org.springframework.data.redis.serializer.RedisSerializationContext.*;

import java.time.Duration;
import java.time.LocalTime;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.moabam.global.common.util.ClockHolder;

import lombok.RequiredArgsConstructor;

@EnableCaching
@Configuration
@RequiredArgsConstructor
public class CacheConfig {

	private final ClockHolder clockHolder;

	@Bean
	public RedisCacheConfiguration redisCacheConfiguration() {
		var strSerializePair = SerializationPair.fromSerializer(new StringRedisSerializer());
		var objSerializePair = SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer(objectMapper()));

		return RedisCacheConfiguration.defaultCacheConfig()
			.entryTtl(getTtl())
			.serializeKeysWith(strSerializePair)
			.serializeValuesWith(objSerializePair);
	}

	private Duration getTtl() {
		LocalTime now = clockHolder.time();
		LocalTime end = clockHolder.endOfDay();

		return Duration.between(now, end);
	}

	private ObjectMapper objectMapper() {
		PolymorphicTypeValidator polymorphicTypeValidator = BasicPolymorphicTypeValidator.builder()
			.allowIfSubType(Object.class)
			.build();

		return JsonMapper.builder()
			.polymorphicTypeValidator(polymorphicTypeValidator)
			.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
			.addModule(new JavaTimeModule())
			.activateDefaultTyping(polymorphicTypeValidator, ObjectMapper.DefaultTyping.NON_FINAL)
			.build();
	}
}
