package com.moabam.api.domain.entity;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.moabam.api.domain.entity.enums.CouponType;
import com.moabam.global.error.exception.BadRequestException;
import com.moabam.global.error.model.ErrorMessage;
import com.moabam.support.fixture.CouponFixture;

class CouponTest {

	@DisplayName("쿠폰이 정상적으로 생성된다. - Coupon")
	@Test
	void coupon() {
		// Given
		LocalDateTime startAt = LocalDateTime.of(2000, 1, 22, 10, 30, 0);
		LocalDateTime endAt = LocalDateTime.of(2000, 1, 22, 11, 0, 0);

		// When
		Coupon actual = Coupon.builder()
			.name("couponName")
			.point(10)
			.type(CouponType.MORNING_COUPON)
			.stock(100)
			.startAt(startAt)
			.endAt(endAt)
			.adminId(1L)
			.build();

		// Then
		assertThat(actual.getName()).isEqualTo("couponName");
		assertThat(actual.getDescription()).isNull();
		assertThat(actual.getPoint()).isEqualTo(10);
		assertThat(actual.getStock()).isEqualTo(100);
		assertThat(actual.getType()).isEqualTo(CouponType.MORNING_COUPON);
		assertThat(actual.getStartAt()).isEqualTo(startAt);
		assertThat(actual.getEndAt()).isEqualTo(endAt);
		assertThat(actual.getAdminId()).isEqualTo(1L);
	}

	@DisplayName("쿠폰 보너스 포인트가 1보다 작다. - BadRequestException")
	@Test
	void coupon_validatePoint_Point_BadRequestException() {
		// When& Then
		assertThatThrownBy(() -> CouponFixture.coupon(0, 1))
			.isInstanceOf(BadRequestException.class)
			.hasMessage(ErrorMessage.INVALID_COUPON_POINT.getMessage());
	}

	@DisplayName("쿠폰 재고가 1보다 작다. - BadRequestException")
	@Test
	void coupon_validatePoint_Stock_BadRequestException() {
		// When& Then
		assertThatThrownBy(() -> CouponFixture.coupon(1, 0))
			.isInstanceOf(BadRequestException.class)
			.hasMessage(ErrorMessage.INVALID_COUPON_STOCK.getMessage());
	}
}
