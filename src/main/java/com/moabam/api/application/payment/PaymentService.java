package com.moabam.api.application.payment;

import static com.moabam.global.error.model.ErrorMessage.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.moabam.api.domain.payment.Payment;
import com.moabam.api.domain.payment.repository.PaymentRepository;
import com.moabam.api.dto.payment.PaymentRequest;
import com.moabam.global.error.exception.NotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PaymentService {

	private final PaymentRepository paymentRepository;

	@Transactional
	public void request(Long memberId, Long paymentId, PaymentRequest request) {
		Payment payment = getById(paymentId);
		payment.validateByMember(memberId);
		payment.request(request.orderId());
	}

	private Payment getById(Long paymentId) {
		return paymentRepository.findById(paymentId)
			.orElseThrow(() -> new NotFoundException(PAYMENT_NOT_FOUND));
	}
}
