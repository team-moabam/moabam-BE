package com.moabam.global.common.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BaseImageUrl {

	public static final String IMAGE_DOMAIN = "https://image.moabam.com/";

	public static final String DEFAULT_SKIN_URL = "";
	public static final String DEFAULT_MORNING_AWAKE_SKIN_URL = "";
	public static final String DEFAULT_MORNING_SLEEP_SKIN_URL = "";
	public static final String DEFAULT_NIGHT_AWAKE_SKIN_URL = "";
	public static final String DEFAULT_NIGHT_SLEEP_SKIN_URL = "";

	public static final String DEFAULT_MORNING_EGG_URL = "moabam/skins/오목눈이/기본/오목눈이알.png";
	public static final String DEFAULT_NIGHT_EGG_URL = "moabam/skins/부엉이/기본/부엉이알.png";
	public static final String MEMBER_PROFILE_URL = "moabam/default/기본회원프로필.png";
}
