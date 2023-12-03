package com.moabam.api.domain.payment;

import static com.moabam.support.fixture.CouponFixture.*;
import static com.moabam.support.fixture.PaymentFixture.*;
import static com.moabam.support.fixture.ProductFixture.*;
import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.moabam.api.domain.coupon.Coupon;
import com.moabam.api.domain.coupon.CouponWallet;
import com.moabam.global.error.exception.BadRequestException;

class PaymentTest {

	@DisplayName("금액이 음수이면 예외가 발생한다.")
	@Test
	void validate_amount_exception() {
		Payment.PaymentBuilder paymentBuilder = Payment.builder()
			.memberId(1L)
			.product(bugProduct())
			.order(order(bugProduct()))
			.totalAmount(-1000);

		assertThatThrownBy(paymentBuilder::build)
			.isInstanceOf(BadRequestException.class)
			.hasMessage("결제 금액은 0 이상이어야 합니다.");
	}

	@DisplayName("쿠폰을 적용한다.")
	@Nested
	class ApplyCoupon {

		@DisplayName("성공한다.")
		@Test
		void success() {
			// given
			Payment payment = payment(bugProduct());
			Coupon coupon = discount1000Coupon();
			CouponWallet couponWallet = CouponWallet.create(1L, coupon);

			// when
			payment.applyCoupon(couponWallet);

			// then
			assertThat(payment.getTotalAmount()).isEqualTo(BUG_PRODUCT_PRICE - 1000);
			assertThat(payment.getDiscountAmount()).isEqualTo(coupon.getPoint());
		}

		@DisplayName("할인 금액이 더 크면 0으로 처리한다.")
		@Test
		void discount_amount_greater() {
			// given
			Payment payment = payment(bugProduct());
			Coupon coupon = discount10000Coupon();
			CouponWallet couponWallet = CouponWallet.create(1L, coupon);

			// when
			payment.applyCoupon(couponWallet);

			// then
			assertThat(payment.getTotalAmount()).isZero();
		}
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
	}
}
