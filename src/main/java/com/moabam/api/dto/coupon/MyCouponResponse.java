package com.moabam.api.dto.coupon;

import com.moabam.api.domain.coupon.CouponType;

import lombok.Builder;

@Builder
public record MyCouponResponse(
	Long id,
	String name,
	String description,
	int point,
	CouponType type
) {

}
