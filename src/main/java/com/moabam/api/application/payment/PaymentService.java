package com.moabam.api.application.payment;

import static com.moabam.global.error.model.ErrorMessage.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.moabam.api.application.bug.BugService;
import com.moabam.api.application.coupon.CouponService;
import com.moabam.api.domain.payment.Payment;
import com.moabam.api.domain.payment.repository.PaymentRepository;
import com.moabam.api.domain.payment.repository.PaymentSearchRepository;
import com.moabam.api.dto.payment.ConfirmPaymentRequest;
import com.moabam.api.dto.payment.ConfirmTossPaymentResponse;
import com.moabam.api.dto.payment.PaymentRequest;
import com.moabam.api.infrastructure.payment.TossPaymentMapper;
import com.moabam.api.infrastructure.payment.TossPaymentService;
import com.moabam.global.error.exception.MoabamException;
import com.moabam.global.error.exception.NotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PaymentService {

	private final BugService bugService;
	private final CouponService couponService;
	private final TossPaymentService tossPaymentService;
	private final PaymentRepository paymentRepository;
	private final PaymentSearchRepository paymentSearchRepository;

	@Transactional
	public void request(Long memberId, Long paymentId, PaymentRequest request) {
		Payment payment = getById(paymentId);
		payment.validateByMember(memberId);
		payment.request(request.orderId());
	}

	@Transactional
	public void confirm(Long memberId, ConfirmPaymentRequest request) {
		Payment payment = getByOrderId(request.orderId());
		payment.validateInfo(memberId, request.amount());

		try {
			ConfirmTossPaymentResponse response = tossPaymentService.confirm(
				TossPaymentMapper.toConfirmRequest(request.paymentKey(), request.orderId(), request.amount())
			);
			payment.confirm(response.paymentKey(), response.approvedAt());

			if (payment.isCouponApplied()) {
				couponService.use(memberId, payment.getCouponWalletId());
			}
			bugService.charge(memberId, payment.getProduct());
		} catch (MoabamException exception) {
			payment.fail(request.paymentKey());
		}
	}

	private Payment getById(Long paymentId) {
		return paymentRepository.findById(paymentId)
			.orElseThrow(() -> new NotFoundException(PAYMENT_NOT_FOUND));
	}

	private Payment getByOrderId(String orderId) {
		return paymentSearchRepository.findByOrderId(orderId)
			.orElseThrow(() -> new NotFoundException(PAYMENT_NOT_FOUND));
	}
}
