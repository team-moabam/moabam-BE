package com.moabam.global.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.moabam.global.error.exception.MoabamException;
import com.moabam.global.error.model.ErrorMessage;

import jakarta.annotation.PreDestroy;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import redis.embedded.RedisServer;

@Slf4j
@Configuration
@Profile("test")
public class EmbeddedRedisConfig {

	private final int redisPort;
	private final String redisHost;

	private int availablePort;
	private RedisServer redisServer;

	public EmbeddedRedisConfig(
		@Value("${spring.data.redis.port}") int redisPort,
		@Value("${spring.data.redis.host}") String redisHost
	) {
		this.redisPort = redisPort;
		this.redisHost = redisHost;

		startRedis();
	}

	@Bean
	public RedisConnectionFactory redisConnectionFactory(EmbeddedRedisConfig embeddedRedisConfig) {
		return new LettuceConnectionFactory(redisHost, embeddedRedisConfig.getAvailablePort());
	}

	@Bean
	public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
		RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(String.class));
		redisTemplate.setHashKeySerializer(new StringRedisSerializer());
		redisTemplate.setConnectionFactory(redisConnectionFactory);

		return redisTemplate;
	}

	@Bean
	public ObjectMapper objectMapper() {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModules(new JavaTimeModule());

		return objectMapper;
	}

	public void startRedis() {
		Os os = Os.createOs();
		availablePort = findPort(os);

		if (os.isMac()) {
			redisServer = new RedisServer(getRedisFileForArcMac(), availablePort);
		} else {
			redisServer = RedisServer.builder()
				.port(availablePort)
				.setting("maxmemory 128M")
				.build();
		}

		try {
			redisServer.start();
		} catch (Exception e) {
			stopRedis();
			throw new MoabamException(e.getMessage());
		}
	}

	@PreDestroy
	public void stopRedis() {
		try {
			if (redisServer != null) {
				redisServer.stop();
			}
		} catch (Exception e) {
			throw new MoabamException(e.getMessage());
		}
	}

	public int getAvailablePort() {
		return availablePort;
	}

	private int findPort(Os os) {
		if (!isRunning(os.executeCommand(redisPort))) {
			return redisPort;
		}

		return findAvailablePort(os);
	}

	private int findAvailablePort(Os os) {
		for (int port = 10000; port <= 65535; port++) {
			Process process = os.executeCommand(port);

			if (!isRunning(process)) {
				return port;
			}
		}

		throw new MoabamException(ErrorMessage.NOT_FOUND_AVAILABLE_PORT);
	}

	private boolean isRunning(Process process) {
		String line;
		StringBuilder pidInfo = new StringBuilder();

		try (BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
			while ((line = input.readLine()) != null) {
				pidInfo.append(line);
			}
		} catch (Exception e) {
			throw new MoabamException(ErrorMessage.ERROR_EXECUTING_EMBEDDED_REDIS);
		}

		return StringUtils.hasText(pidInfo.toString());
	}

	private File getRedisFileForArcMac() {
		try {
			return new ClassPathResource("binary/redis/redis-server-arm64").getFile();
		} catch (Exception e) {
			throw new MoabamException(e.getMessage());
		}
	}

	private static final class Os {

		enum Type {
			MAC,
			WIN,
			LINUX
		}

		private final String shellPath;
		private final String optionOperator;
		private final String command;
		private final Type type;

		@Builder
		private Os(String shellPath, String optionOperator, String command, Type type) {
			this.shellPath = shellPath;
			this.optionOperator = optionOperator;
			this.command = command;
			this.type = type;
		}

		public Process executeCommand(int port) {
			String osCommand = String.format(this.command, port);
			String[] script = {shellPath, optionOperator, osCommand};

			try {
				return Runtime.getRuntime().exec(script);
			} catch (IOException e) {
				throw new MoabamException(e.getMessage());
			}
		}

		public boolean isMac() {
			return type == Type.MAC;
		}

		public static Os createOs() {
			String osArchitecture = System.getProperty("os.arch");
			String osName = System.getProperty("os.name");

			if (osArchitecture.equals("aarch64") && osName.equals("Mac OS X")) {
				return linuxOs(Type.MAC);
			}

			if (osArchitecture.equals("amd64") && osName.contains("Windows")) {
				return windowOs();
			}

			return linuxOs(Type.LINUX);
		}

		// 변경 전
		private static Os linuxOs(Type type) {
			return Os.builder()
				.shellPath("/bin/sh")
				.optionOperator("-c")
				.command("netstat -nat | grep LISTEN | grep %d")
				.type(type)
				.build();
		}

		// 변경 후
		private static Os windowOs() {
			return Os.builder()
				.shellPath("cmd.exe")
				.optionOperator("/c")
				.command("netstat -ano | findstr LISTEN | findstr %d")
				.type(Type.WIN)
				.build();
		}
	}
}
