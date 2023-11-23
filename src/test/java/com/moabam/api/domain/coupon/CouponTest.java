package com.moabam.api.domain.coupon;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.moabam.global.error.exception.BadRequestException;
import com.moabam.global.error.model.ErrorMessage;
import com.moabam.support.fixture.CouponFixture;

class CouponTest {

	@DisplayName("쿠폰이 성공적으로 생성된다. - Coupon")
	@Test
	void coupon_success() {
		// Given
		LocalDate startAt = LocalDate.of(2023, 2, 1);
		LocalDate openAt = LocalDate.of(2023, 1, 1);

		// When
		Coupon actual = Coupon.builder()
			.name("couponName")
			.point(10)
			.type(CouponType.MORNING_COUPON)
			.stock(100)
			.startAt(startAt)
			.openAt(openAt)
			.adminId(1L)
			.build();

		// Then
		assertThat(actual.getName()).isEqualTo("couponName");
		assertThat(actual.getDescription()).isBlank();
		assertThat(actual.getPoint()).isEqualTo(10);
		assertThat(actual.getStock()).isEqualTo(100);
		assertThat(actual.getType()).isEqualTo(CouponType.MORNING_COUPON);
		assertThat(actual.getStartAt()).isEqualTo(startAt);
		assertThat(actual.getOpenAt()).isEqualTo(openAt);
		assertThat(actual.getAdminId()).isEqualTo(1L);
	}

	@DisplayName("쿠폰 보너스 포인트가 1보다 작다. - BadRequestException")
	@Test
	void validatePoint_BadRequestException() {
		// When& Then
		assertThatThrownBy(() -> CouponFixture.coupon(0, 1))
			.isInstanceOf(BadRequestException.class)
			.hasMessage(ErrorMessage.INVALID_COUPON_POINT.getMessage());
	}

	@DisplayName("쿠폰 재고가 1보다 작다. - BadRequestException")
	@Test
	void validateStock_BadRequestException() {
		// When& Then
		assertThatThrownBy(() -> CouponFixture.coupon(1, 0))
			.isInstanceOf(BadRequestException.class)
			.hasMessage(ErrorMessage.INVALID_COUPON_STOCK.getMessage());
	}
}
