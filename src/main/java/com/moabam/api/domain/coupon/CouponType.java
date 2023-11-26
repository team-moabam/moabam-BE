package com.moabam.api.domain.coupon;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.moabam.global.error.exception.BadRequestException;
import com.moabam.global.error.exception.NotFoundException;
import com.moabam.global.error.model.ErrorMessage;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum CouponType {

	MORNING("아침"),
	NIGHT("저녁"),
	GOLDEN("황금"),
	DISCOUNT("할인");

	private final String name;
	private static final Map<String, CouponType> COUPON_TYPE_MAP;

	static {
		COUPON_TYPE_MAP = Collections.unmodifiableMap(Arrays.stream(values())
			.collect(Collectors.toMap(CouponType::getName, Function.identity())));
	}

	public static CouponType from(String name) {
		return Optional.ofNullable(COUPON_TYPE_MAP.get(name))
			.orElseThrow(() -> new NotFoundException(ErrorMessage.NOT_FOUND_COUPON_TYPE));
	}

	public void validateNotDiscount() {
		if (this == CouponType.DISCOUNT) {
			throw new BadRequestException(ErrorMessage.INVALID_DISCOUNT_COUPON);
		}
	}
}
