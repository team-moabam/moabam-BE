package com.moabam.global.common.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GlobalConstant {

	public static final String BLANK = "";
	public static final String COMMA = ",";
	public static final String UNDER_BAR = "_";
	public static final String CHARSET_UTF_8 = ";charset=UTF-8";

	public static final String TO = "_TO_";
	public static final long EXPIRE_KNOCK = 12;
	public static final long EXPIRE_FCM_TOKEN = 60;
	public static final String FIREBASE_PATH = "config/moabam-firebase.json";

	public static final String REDIS_SERVER_MAX_MEMORY = "maxmemory 128M";
	public static final String REDIS_BINARY_PATH = "binary/redis/redis-server-arm64";
	public static final String FIND_LISTEN_PROCESS_COMMAND = "netstat -nat | grep LISTEN | grep %d";
	public static final String SHELL_PATH = "/bin/sh";
	public static final String SHELL_COMMAND_OPTION = "-c";
	public static final String OS_ARCHITECTURE = "os.arch";
	public static final String OS_NAME = "os.name";
	public static final String ARM_ARCHITECTURE = "aarch64";
	public static final String MAC_OS_NAME = "Mac OS X";
}
