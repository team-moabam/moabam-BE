package com.moabam.support.fixture;

import java.time.LocalDateTime;

import com.moabam.api.domain.entity.Coupon;
import com.moabam.api.domain.entity.enums.CouponType;

public final class CouponFixture {

	public static Coupon coupon(int point, int stock) {
		return Coupon.builder()
			.name("couponName")
			.point(point)
			.type(CouponType.MORNING_COUPON)
			.stock(stock)
			.startAt(LocalDateTime.now())
			.endAt(LocalDateTime.now())
			.build();
	}
}
