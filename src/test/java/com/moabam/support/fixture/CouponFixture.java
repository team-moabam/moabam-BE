package com.moabam.support.fixture;

import java.time.LocalDateTime;

import com.moabam.api.domain.entity.Coupon;
import com.moabam.api.domain.entity.enums.CouponType;
import com.moabam.api.dto.CreateCouponRequest;

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

	public static CreateCouponRequest createCouponRequest(String couponType, int startMonth, int endMonth) {
		return CreateCouponRequest.builder()
			.name("couponName")
			.description("coupon description")
			.point(10)
			.type(couponType)
			.stock(10)
			.startAt(LocalDateTime.of(2000, startMonth, 22, 10, 30, 0))
			.endAt(LocalDateTime.of(2000, endMonth, 22, 11, 0, 0))
			.build();
	}
}
