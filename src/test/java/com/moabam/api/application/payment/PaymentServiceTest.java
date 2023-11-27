package com.moabam.api.application.payment;

import static com.moabam.support.fixture.CouponFixture.*;
import static com.moabam.support.fixture.PaymentFixture.*;
import static com.moabam.support.fixture.ProductFixture.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.moabam.api.application.bug.BugService;
import com.moabam.api.application.coupon.CouponService;
import com.moabam.api.domain.payment.Payment;
import com.moabam.api.domain.payment.PaymentStatus;
import com.moabam.api.domain.payment.repository.PaymentRepository;
import com.moabam.api.domain.payment.repository.PaymentSearchRepository;
import com.moabam.api.dto.payment.ConfirmPaymentRequest;
import com.moabam.api.dto.payment.PaymentRequest;
import com.moabam.api.infrastructure.payment.TossPaymentService;
import com.moabam.global.error.exception.MoabamException;
import com.moabam.global.error.exception.NotFoundException;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

	@InjectMocks
	PaymentService paymentService;

	@Mock
	BugService bugService;

	@Mock
	CouponService couponService;

	@Mock
	TossPaymentService tossPaymentService;

	@Mock
	PaymentRepository paymentRepository;

	@Mock
	PaymentSearchRepository paymentSearchRepository;

	@DisplayName("결제를 요청한다.")
	@Nested
	class Request {

		@DisplayName("해당 결제 정보가 존재하지 않으면 예외가 발생한다.")
		@Test
		void not_found_exception() {
			// given
			Long memberId = 1L;
			Long paymentId = 1L;
			PaymentRequest request = new PaymentRequest(ORDER_ID);
			given(paymentRepository.findById(paymentId)).willReturn(Optional.empty());

			// when, then
			assertThatThrownBy(() -> paymentService.request(memberId, paymentId, request))
				.isInstanceOf(NotFoundException.class)
				.hasMessage("존재하지 않는 결제 정보입니다.");
		}
	}

	@DisplayName("결제를 승인한다.")
	@Nested
	class Confirm {

		@DisplayName("해당 결제 정보가 존재하지 않으면 예외가 발생한다.")
		@Test
		void not_found_exception() {
			// given
			Long memberId = 1L;
			ConfirmPaymentRequest request = confirmPaymentRequest();
			given(paymentSearchRepository.findByOrderId(request.orderId())).willReturn(Optional.empty());

			// when, then
			assertThatThrownBy(() -> paymentService.confirm(memberId, request))
				.isInstanceOf(NotFoundException.class)
				.hasMessage("존재하지 않는 결제 정보입니다.");
		}

		@DisplayName("쿠폰을 적용한 경우 쿠폰을 차감한 후 벌레를 충전한다.")
		@Test
		void use_coupon_success() {
			// given
			Long memberId = 1L;
			Long couponWalletId = 1L;
			Payment payment = paymentWithCoupon(bugProduct(), discount1000Coupon(), couponWalletId);
			ConfirmPaymentRequest request = confirmPaymentRequest();
			given(paymentSearchRepository.findByOrderId(request.orderId())).willReturn(Optional.of(payment));
			given(tossPaymentService.confirm(confirmTossPaymentRequest())).willReturn(confirmTossPaymentResponse());

			// when
			paymentService.confirm(memberId, request);

			// then
			verify(couponService, times(1)).discount(memberId, couponWalletId);
			verify(bugService, times(1)).charge(memberId, payment.getProduct());
		}

		@DisplayName("실패한다.")
		@Test
		void fail() {
			// given
			Long memberId = 1L;
			Long couponWalletId = 1L;
			Payment payment = paymentWithCoupon(bugProduct(), discount1000Coupon(), couponWalletId);
			ConfirmPaymentRequest request = confirmPaymentRequest();
			given(paymentSearchRepository.findByOrderId(request.orderId())).willReturn(Optional.of(payment));
			given(tossPaymentService.confirm(any())).willThrow(MoabamException.class);

			// when
			paymentService.confirm(memberId, request);

			// then
			assertThat(payment.getStatus()).isEqualTo(PaymentStatus.ABORTED);
		}
	}
}
