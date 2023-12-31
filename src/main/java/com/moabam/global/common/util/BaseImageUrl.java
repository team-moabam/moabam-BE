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

	public static final String DEFAULT_MORNING_EGG_URL = "moabam/skins/omok/default/egg.png";
	public static final String DEFAULT_NIGHT_EGG_URL = "moabam/skins/owl/default/egg.png";
	public static final String MEMBER_PROFILE_URL = "moabam/default/member-profile.png";
}
