package com.moabam.global.common.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GlobalConstant {

	public static final String BLANK = "";
	public static final String DELIMITER = "/";
	public static final String CHARSET_UTF_8 = ";charset=UTF-8";
	public static final String SPACE = " ";
	public static final int MIDNIGHT_HOUR = 0;
	public static final int ONE_HOUR = 1;
	public static final int HOURS_IN_A_DAY = 24;
	public static final int NOT_COMPLETED_RANK = 500;

	public static final int ROOM_FIXED_SEARCH_SIZE = 10;
	public static final int LEVEL_DIVISOR = 10;
	public static final String IMAGE_EXTENSION = ".png";
}
