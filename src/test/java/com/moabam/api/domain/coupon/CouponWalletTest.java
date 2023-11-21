package com.moabam.api.domain.coupon;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.moabam.support.fixture.CouponFixture;

class CouponWalletTest {

	@DisplayName("쿠폰 지갑 엔티티를 생성한다. - Void")
	@Test
	void couponWallet() {
		// Given
		Coupon coupon = CouponFixture.coupon("CouponName", 1, 2);

		// When
		CouponWallet actual = CouponWallet.builder()
			.memberId(1L)
			.coupon(coupon)
			.build();

		// Then
		assertThat(actual.getMemberId()).isEqualTo(1L);
		assertThat(actual.getCoupon().getName()).isEqualTo(coupon.getName());
	}
}
