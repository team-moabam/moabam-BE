package com.moabam.global.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.moabam.global.config.EmbeddedRedisConfig;

@Configuration
@Profile("test")
public class TestConfig {

	@Value("${spring.data.redis.host}")
	private String redisHost;

	@Bean
	public RedisConnectionFactory redisConnectionFactory(EmbeddedRedisConfig embeddedRedisConfig) {
		return new LettuceConnectionFactory(redisHost, embeddedRedisConfig.getAvailablePort());
	}

	@Bean
	public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
		StringRedisTemplate stringRedisTemplate = new StringRedisTemplate();
		stringRedisTemplate.setKeySerializer(new StringRedisSerializer());
		stringRedisTemplate.setValueSerializer(new StringRedisSerializer());
		stringRedisTemplate.setConnectionFactory(redisConnectionFactory);

		return stringRedisTemplate;
	}
}
