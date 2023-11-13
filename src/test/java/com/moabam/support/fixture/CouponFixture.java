package com.moabam.support.fixture;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.provider.Arguments;

import com.moabam.api.domain.entity.Coupon;
import com.moabam.api.domain.entity.enums.CouponType;
import com.moabam.api.dto.CouponSearchRequest;
import com.moabam.api.dto.CreateCouponRequest;

public final class CouponFixture {

	public static Coupon coupon(int point, int stock) {
		return Coupon.builder()
			.name("couponName")
			.point(point)
			.couponType(CouponType.MORNING_COUPON)
			.stock(stock)
			.startAt(LocalDateTime.of(2023, 1, 1, 0, 0))
			.endAt(LocalDateTime.of(2023, 1, 1, 0, 0))
			.adminId(1L)
			.build();
	}

	public static Coupon coupon(String name, int startMonth, int endMonth) {
		return Coupon.builder()
			.name(name)
			.point(10)
			.couponType(CouponType.MORNING_COUPON)
			.stock(100)
			.startAt(LocalDateTime.of(2023, startMonth, 1, 0, 0))
			.endAt(LocalDateTime.of(2023, endMonth, 1, 0, 0))
			.adminId(1L)
			.build();
	}

	public static CreateCouponRequest createCouponRequest(String couponType, int startMonth, int endMonth) {
		return CreateCouponRequest.builder()
			.name("couponName")
			.description("coupon description")
			.point(10)
			.couponType(couponType)
			.stock(10)
			.startAt(LocalDateTime.of(2023, startMonth, 1, 0, 0))
			.endAt(LocalDateTime.of(2023, endMonth, 1, 0, 0))
			.build();
	}

	public static CouponSearchRequest couponSearchRequest(boolean ongoing, boolean notStarted, boolean ended) {
		return CouponSearchRequest.builder()
			.couponOngoing(ongoing)
			.couponNotStarted(notStarted)
			.couponEnded(ended)
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
