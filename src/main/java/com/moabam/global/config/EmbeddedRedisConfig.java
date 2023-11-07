package com.moabam.global.config;

import static com.moabam.global.common.constant.RedisConstant.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StringUtils;

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

	@Value("${spring.data.redis.port}")
	private int redisPort;

	private int availablePort;
	private RedisServer redisServer;

	public EmbeddedRedisConfig(@Value("${spring.data.redis.port}") int redisPort) {
		this.redisPort = redisPort;

		startRedis();
	}

	public void startRedis() {
		Os os = Os.createOs();
		availablePort = findPort(os);

		if (os.isMac()) {
			redisServer = new RedisServer(getRedisFileForArcMac(), availablePort);
		} else {
			redisServer = RedisServer.builder()
				.port(availablePort)
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
			return new ClassPathResource(REDIS_BINARY_PATH).getFile();
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
			String command = String.format(this.command, port);
			String[] script = {shellPath, optionOperator, command};

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
			String osArchitecture = System.getProperty(OS_ARCHITECTURE);
			String osName = System.getProperty(OS_NAME);

			if (osArchitecture.equals(ARM_ARCHITECTURE) && osName.equals(MAC_OS_NAME)) {
				return linuxOs(Type.MAC);
			}

			if (osArchitecture.equals(AMD_ARCHITECTURE) && osName.contains(WINDOW_OS_NAME)) {
				return windowOs();
			}

			return linuxOs(Type.LINUX);
		}

		private static Os linuxOs(Type type) {
			return Os.builder()
				.shellPath(SHELL_PATH)
				.optionOperator(SHELL_COMMAND_OPTION)
				.command(FIND_LISTEN_PROCESS_COMMAND)
				.type(type)
				.build();
		}

		private static Os windowOs() {
			return Os.builder()
				.shellPath(WIN_SHELL_PATH)
				.optionOperator(WIN_OPTION_OPERATOR)
				.command(WIN_LISTEN_PROCESS_COMMAND)
				.type(Type.WIN)
				.build();
		}
	}
}
