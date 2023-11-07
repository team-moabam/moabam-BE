package com.moabam.global.common.constant;

public class RedisConstant {

	public static final String REDIS_SERVER_MAX_MEMORY = "maxmemory 128M";
	public static final String REDIS_BINARY_PATH = "binary/redis/redis-server-arm64";
	public static final String FIND_LISTEN_PROCESS_COMMAND = "netstat -nat | grep LISTEN | grep %d";
	public static final String WIN_LISTEN_PROCESS_COMMAND = "netstat -ano | findstr LISTEN | findstr %d";

	public static final String SHELL_PATH = "/bin/sh";
	public static final String WIN_SHELL_PATH = "cmd.exe";
	public static final String SHELL_COMMAND_OPTION = "-c";
	public static final String WIN_OPTION_OPERATOR = "/c";

	public static final String OS_ARCHITECTURE = "os.arch";
	public static final String OS_NAME = "os.name";
	public static final String ARM_ARCHITECTURE = "aarch64";
	public static final String AMD_ARCHITECTURE = "amd64";
	public static final String WINDOW_OS_NAME = "Windows";
	public static final String MAC_OS_NAME = "Mac OS X";
}
