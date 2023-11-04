package com.moabam.global.config;

import static com.moabam.global.common.util.GlobalConstant.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StringUtils;

import com.moabam.global.error.exception.MoabamException;
import com.moabam.global.error.model.ErrorMessage;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import redis.embedded.RedisServer;

@Slf4j
@Configuration
@Profile("test")
public class EmbeddedRedisConfig {

	@Value("${spring.data.redis.port}")
	private int redisPort;

	private RedisServer redisServer;

	@PostConstruct
	public void startRedis() {
		int port = isRedisRunning() ? findAvailablePort() : redisPort;

		if (isArmMac()) {
			redisServer = new RedisServer(getRedisFileForArcMac(), port);
		} else {
			redisServer = RedisServer.builder()
				.port(port)
				.setting(REDIS_SERVER_MAX_MEMORY)
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

	public int findAvailablePort() {
		for (int port = 10000; port <= 65535; port++) {
			Process process = executeGrepProcessCommand(port);

			if (!isRunning(process)) {
				return port;
			}
		}

		throw new MoabamException(ErrorMessage.NOT_FOUND_AVAILABLE_PORT);
	}

	private boolean isRedisRunning() {
		return isRunning(executeGrepProcessCommand(redisPort));
	}

	private Process executeGrepProcessCommand(int redisPort) {
		String command = String.format(FIND_LISTEN_PROCESS_COMMAND, redisPort);
		String[] shell = {SHELL_PATH, SHELL_COMMAND_OPTION, command};

		try {
			return Runtime.getRuntime().exec(shell);
		} catch (IOException e) {
			throw new MoabamException(e.getMessage());
		}
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

	private boolean isArmMac() {
		return Objects.equals(System.getProperty(OS_ARCHITECTURE), ARM_ARCHITECTURE)
			&& Objects.equals(System.getProperty(OS_NAME), MAC_OS_NAME);
	}

	private File getRedisFileForArcMac() {
		try {
			return new ClassPathResource(REDIS_BINARY_PATH).getFile();
		} catch (Exception e) {
			throw new MoabamException(e.getMessage());
		}
	}
}
