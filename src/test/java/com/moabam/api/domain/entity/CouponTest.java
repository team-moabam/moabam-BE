package com.moabam.api.domain.entity;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.moabam.global.error.exception.BadRequestException;
import com.moabam.global.error.model.ErrorMessage;
import com.moabam.support.fixture.CouponFixture;

class CouponTest {

	@DisplayName("쿠폰 보너스 포인트가 0보다 작을 때, - BadRequestException")
	@Test
	void coupon_validatePoint_Point_BadRequestException() {
		// When& Then
		assertThatThrownBy(() -> CouponFixture.coupon(0, 1))
			.isInstanceOf(BadRequestException.class)
			.hasMessage(ErrorMessage.INVALID_COUPON_POINT.getMessage());
	}

	@DisplayName("쿠폰 재고가 0보다 작을 때, - BadRequestException")
	@Test
	void coupon_validatePoint_Stock_BadRequestException() {
		// When& Then
		assertThatThrownBy(() -> CouponFixture.coupon(1, 0))
			.isInstanceOf(BadRequestException.class)
			.hasMessage(ErrorMessage.INVALID_COUPON_STOCK.getMessage());
	}
}
