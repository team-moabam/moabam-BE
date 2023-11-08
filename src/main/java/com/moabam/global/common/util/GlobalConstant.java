package com.moabam.global.common.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GlobalConstant {

	public static final String BLANK = "";
	public static final String COMMA = ",";
	public static final String UNDER_BAR = "_";
	public static final String CHARSET_UTF_8 = ";charset=UTF-8";
	public static final String SPACE = " ";

	public static final String TO = "_TO_";
	public static final long EXPIRE_KNOCK = 12;
	public static final long EXPIRE_FCM_TOKEN = 60;
	public static final String FIREBASE_PATH = "config/moabam-firebase.json";

	public static final int LEVEL_DIVISOR = 10;
}
