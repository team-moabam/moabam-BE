package com.moabam.support.fixture;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.params.provider.Arguments;

import com.moabam.api.domain.coupon.Coupon;
import com.moabam.api.domain.coupon.CouponType;
import com.moabam.api.dto.coupon.CouponStatusRequest;
import com.moabam.api.dto.coupon.CreateCouponRequest;

public final class CouponFixture {

	public static final String DISCOUNT_1000_COUPON_NAME = "황금벌레 1000원 할인";
	public static final String DISCOUNT_10000_COUPON_NAME = "황금벌레 10000원 할인";

	public static Coupon coupon() {
		return Coupon.builder()
			.name("couponName")
			.point(1000)
			.type(CouponType.MORNING)
			.maxCount(100)
			.startAt(LocalDate.of(2023, 2, 1))
			.openAt(LocalDate.of(2023, 1, 1))
			.adminId(1L)
			.build();
	}

	public static Coupon coupon(String name, int startAt) {
		return Coupon.builder()
			.name(name)
			.point(10)
			.type(CouponType.MORNING)
			.maxCount(100)
			.startAt(LocalDate.of(2023, startAt, 1))
			.openAt(LocalDate.of(2023, 1, 1))
			.adminId(1L)
			.build();
	}

	public static Coupon coupon(int point, int maxCount) {
		return Coupon.builder()
			.name("couponName")
			.point(point)
			.type(CouponType.MORNING)
			.maxCount(maxCount)
			.startAt(LocalDate.of(2023, 2, 1))
			.openAt(LocalDate.of(2023, 1, 1))
			.adminId(1L)
			.build();
	}

	public static Coupon coupon(CouponType couponType, int point) {
		return Coupon.builder()
			.name("couponName")
			.point(point)
			.type(couponType)
			.maxCount(100)
			.startAt(LocalDate.of(2023, 2, 1))
			.openAt(LocalDate.of(2023, 1, 1))
			.adminId(1L)
			.build();
	}

	public static Coupon coupon(String name, int startMonth, int openMonth) {
		return Coupon.builder()
			.name(name)
			.point(10)
			.type(CouponType.MORNING)
			.maxCount(100)
			.startAt(LocalDate.of(2023, startMonth, 1))
			.openAt(LocalDate.of(2023, openMonth, 1))
			.adminId(1L)
			.build();
	}

	public static Coupon discount1000Coupon() {
		return Coupon.builder()
			.name(DISCOUNT_1000_COUPON_NAME)
			.point(1000)
			.type(CouponType.DISCOUNT)
			.maxCount(100)
			.startAt(LocalDate.of(2023, 2, 1))
			.openAt(LocalDate.of(2023, 1, 1))
			.adminId(1L)
			.build();
	}

	public static Coupon discount10000Coupon() {
		return Coupon.builder()
			.name(DISCOUNT_10000_COUPON_NAME)
			.point(10000)
			.type(CouponType.DISCOUNT)
			.maxCount(100)
			.startAt(LocalDate.of(2023, 2, 1))
			.openAt(LocalDate.of(2023, 2, 1))
			.adminId(1L)
			.build();
	}

	public static CreateCouponRequest createCouponRequest() {
		return CreateCouponRequest.builder()
			.name("couponName")
			.description("coupon description")
			.point(10)
			.type(CouponType.GOLDEN.getName())
			.maxCount(10)
			.startAt(LocalDate.of(2023, 2, 1))
			.openAt(LocalDate.of(2023, 1, 1))
			.build();
	}

	public static CreateCouponRequest createCouponRequest(String couponType, int startMonth, int openMonth) {
		return CreateCouponRequest.builder()
			.name("couponName")
			.description("coupon description")
			.point(10)
			.type(couponType)
			.maxCount(10)
			.startAt(LocalDate.of(2023, startMonth, 1))
			.openAt(LocalDate.of(2023, openMonth, 1))
			.build();
	}

	public static CouponStatusRequest couponStatusRequest(boolean ongoing, boolean ended) {
		return CouponStatusRequest.builder()
			.opened(ongoing)
			.ended(ended)
			.build();
	}

	public static Stream<Arguments> provideCoupons() {
		return Stream.of(Arguments.of(
			List.of(
				coupon("coupon1", 3, 1),
				coupon("coupon2", 4, 2),
				coupon("coupon3", 5, 3),
				coupon("coupon4", 6, 4),
				coupon("coupon5", 7, 5),
				coupon("coupon6", 8, 6),
				coupon("coupon7", 9, 7),
				coupon("coupon8", 10, 8),
				coupon("coupon9", 11, 9),
				coupon("coupon10", 12, 10)
			))
		);
	}

	public static Stream<Arguments> provideValues_Object() {
		Set<Object> values = new HashSet<>();
		values.add(2L);
		values.add(3L);
		values.add(4L);
		values.add(1L);
		values.add(5L);
		values.add(6L);
		values.add(7L);
		values.add(8L);
		values.add(9L);
		values.add(10L);

		return Stream.of(Arguments.of(values));
	}

	public static Stream<Arguments> provideValues_Long() {
		Set<Object> values = new HashSet<>();
		values.add(2L);
		values.add(3L);
		values.add(4L);
		values.add(1L);
		values.add(5L);
		values.add(6L);
		values.add(7L);
		values.add(8L);
		values.add(9L);
		values.add(10L);

		return Stream.of(Arguments.of(values));
	}
}
