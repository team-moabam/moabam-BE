package com.moabam.api.domain.image;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ImageSize {

	CAGE(450),
	BIRD_SKIN(150),
	COUPON_EVENT(420),
	PROFILE_IMAGE(150),
	CERTIFICATION_IMAGE(220);

	private final int width;
}
