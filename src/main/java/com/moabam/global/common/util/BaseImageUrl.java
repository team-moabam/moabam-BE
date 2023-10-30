package com.moabam.global.common.util;

import lombok.Getter;

@Getter
public enum BaseImageUrl {

	PROFILE_URL("/profile/baseUrl");

	private String url;

	BaseImageUrl(String url) {
		this.url = url;
	}
}
