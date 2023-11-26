package com.moabam.api.domain.coupon;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.moabam.global.error.exception.NotFoundException;
import com.moabam.global.error.model.ErrorMessage;

class CouponTypeTest {

	@DisplayName("존재하는 쿠폰을 성공적으로 가져온다. - CouponType")
	@Test
	void from_success() {
		// When
		CouponType actual = CouponType.from(CouponType.GOLDEN.getName());

		// Then
		assertThat(actual).isEqualTo(CouponType.GOLDEN);
	}

	@DisplayName("존재하지 않는 쿠폰을 가져온다. - NotFoundException")
	@Test
	void from_NotFoundException() {
		// When & Then
		assertThatThrownBy(() -> CouponType.from("Not-Coupon"))
			.isInstanceOf(NotFoundException.class)
			.hasMessage(ErrorMessage.NOT_FOUND_COUPON_TYPE.getMessage());
	}
}
