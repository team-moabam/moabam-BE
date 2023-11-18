package com.moabam.api.domain.payment;

import static com.moabam.support.fixture.CouponFixture.*;
import static com.moabam.support.fixture.PaymentFixture.*;
import static com.moabam.support.fixture.ProductFixture.*;
import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.moabam.api.domain.coupon.Coupon;
import com.moabam.global.error.exception.BadRequestException;

class PaymentTest {

	@DisplayName("쿠폰을 적용한다.")
	@Test
	void apply_coupon_success() {
		// given
		Payment payment = payment(bugProduct());
		Coupon coupon = discount1000Coupon();

		// when
		payment.applyCoupon(coupon);

		// then
		assertThat(payment.getOrder().getAmount()).isEqualTo(2000);
		assertThat(payment.getCoupon()).isEqualTo(coupon);
	}

	@DisplayName("해당 회원의 결제 정보가 아니면 예외가 발생한다.")
	@Test
	void validate_by_member_exception() {
		// given
		Long memberId = 2L;
		Payment payment = payment(bugProduct());

		// when, then
		assertThatThrownBy(() -> payment.validateByMember(memberId))
			.isInstanceOf(BadRequestException.class)
			.hasMessage("해당 회원의 결제 정보가 아닙니다.");
	}

	@DisplayName("결제를 요청한다.")
	@Test
	void request_success() {
		// given
		Payment payment = payment(bugProduct());

		// when
		payment.request(ORDER_ID);

		// then
		assertThat(payment.getOrder().getId()).isEqualTo(ORDER_ID);
		assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REQUEST);
	}
}
