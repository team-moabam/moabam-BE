package com.moabam.api.domain.payment;

import static com.moabam.support.fixture.CouponFixture.*;
import static com.moabam.support.fixture.PaymentFixture.*;
import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.moabam.api.domain.coupon.Coupon;

class PaymentTest {

	@DisplayName("쿠폰을 적용한다.")
	@Test
	void apply_coupon() {
		// given
		Payment payment = bugProductPayment();
		Coupon coupon = discount1000Coupon();

		// when
		payment.applyCoupon(coupon);

		// then
		assertThat(payment.getOrder().getAmount()).isEqualTo(2000);
		assertThat(payment.getCoupon()).isEqualTo(coupon);
	}
}
