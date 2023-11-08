package com.moabam.api.domain.entity.enums;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.moabam.global.error.exception.NotFoundException;
import com.moabam.global.error.model.ErrorMessage;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum CouponType {

	MORNING_COUPON("아침"),
	NIGHT_COUPON("저녁"),
	GOLDEN_COUPON("황금"),
	DISCOUNT_COUPON("할인");

	private final String typeName;
	private static final Map<String, CouponType> COUPON_TYPE_MAP;

	static {
		COUPON_TYPE_MAP = Collections.unmodifiableMap(Arrays.stream(values())
			.collect(Collectors.toMap(CouponType::getTypeName, Function.identity())));
	}

	public static CouponType from(String typeName) {
		return Optional.ofNullable(COUPON_TYPE_MAP.get(typeName))
			.orElseThrow(() -> new NotFoundException(ErrorMessage.NOT_FOUND_COUPON_TYPE));
	}
}
