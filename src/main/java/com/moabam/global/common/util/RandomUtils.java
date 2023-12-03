package com.moabam.global.common.util;

import java.security.SecureRandom;

import org.apache.commons.lang3.RandomStringUtils;

public class RandomUtils {

	public static String randomStringValues() {
		return RandomStringUtils.random(6, 0, 0, true, true, null,
			new SecureRandom());
	}

	public static String randomNumberValues() {
		return RandomStringUtils.random(4, 0, 0, false, true, null,
			new SecureRandom());
	}

}
