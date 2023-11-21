package com.moabam.support.fixture;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.provider.Arguments;

import com.moabam.api.domain.coupon.Coupon;
import com.moabam.api.domain.coupon.CouponType;
import com.moabam.api.dto.coupon.CouponStatusRequest;
import com.moabam.api.dto.coupon.CreateCouponRequest;

public final class CouponFixture {

	public static final String DISCOUNT_1000_COUPON_NAME = "황금벌레 1000원 할인";
	public static final String DISCOUNT_10000_COUPON_NAME = "황금벌레 10000원 할인";

	public static Coupon coupon(int point, int stock) {
		return Coupon.builder()
			.name("couponName")
			.point(point)
			.type(CouponType.MORNING_COUPON)
			.stock(stock)
			.startAt(LocalDateTime.of(2023, 1, 1, 0, 0))
			.endAt(LocalDateTime.of(2023, 2, 1, 0, 0))
			.adminId(1L)
			.build();
	}

	public static Coupon coupon(String name, int startMonth, int endMonth) {
		return Coupon.builder()
			.name(name)
			.point(10)
			.type(CouponType.MORNING_COUPON)
			.stock(100)
			.startAt(LocalDateTime.of(2023, startMonth, 1, 0, 0))
			.endAt(LocalDateTime.of(2023, endMonth, 1, 0, 0))
			.adminId(1L)
			.build();
	}

	public static Coupon discount1000Coupon() {
		return Coupon.builder()
			.name(DISCOUNT_1000_COUPON_NAME)
			.point(1000)
			.type(CouponType.DISCOUNT_COUPON)
			.stock(100)
			.startAt(LocalDateTime.of(2023, 1, 1, 0, 0))
			.endAt(LocalDateTime.of(2023, 1, 1, 0, 0))
			.adminId(1L)
			.build();
	}

	public static Coupon discount10000Coupon() {
		return Coupon.builder()
			.name(DISCOUNT_10000_COUPON_NAME)
			.point(10000)
			.type(CouponType.DISCOUNT_COUPON)
			.stock(100)
			.startAt(LocalDateTime.of(2023, 1, 1, 0, 0))
			.endAt(LocalDateTime.of(2023, 1, 1, 0, 0))
			.adminId(1L)
			.build();
	}

	public static CreateCouponRequest createCouponRequest(String couponType, int startMonth, int endMonth) {
		return CreateCouponRequest.builder()
			.name("couponName")
			.description("coupon description")
			.point(10)
			.type(couponType)
			.stock(10)
			.startAt(LocalDateTime.of(2023, startMonth, 1, 0, 0))
			.endAt(LocalDateTime.of(2023, endMonth, 1, 0, 0))
			.build();
	}

	public static CouponStatusRequest couponStatusRequest(boolean ongoing, boolean notStarted, boolean ended) {
		return CouponStatusRequest.builder()
			.ongoing(ongoing)
			.notStarted(notStarted)
			.ended(ended)
			.build();
	}

	public static Stream<Arguments> provideCoupons() {
		return Stream.of(Arguments.of(
			List.of(
				coupon("coupon1", 1, 3),
				coupon("coupon2", 2, 4),
				coupon("coupon3", 3, 5),
				coupon("coupon4", 4, 6),
				coupon("coupon5", 5, 7),
				coupon("coupon6", 6, 8),
				coupon("coupon7", 7, 9),
				coupon("coupon8", 8, 10),
				coupon("coupon9", 9, 11),
				coupon("coupon10", 10, 12)
			))
		);
	}
}
