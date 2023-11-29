package com.moabam.api.presentation;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.moabam.api.application.payment.PaymentService;
import com.moabam.api.domain.payment.Payment;
import com.moabam.api.dto.payment.ConfirmPaymentRequest;
import com.moabam.api.dto.payment.ConfirmTossPaymentResponse;
import com.moabam.api.dto.payment.PaymentRequest;
import com.moabam.api.infrastructure.payment.TossPaymentService;
import com.moabam.global.auth.annotation.Auth;
import com.moabam.global.auth.model.AuthMember;
import com.moabam.global.error.exception.TossPaymentException;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

	private final PaymentService paymentService;
	private final TossPaymentService tossPaymentService;

	@PostMapping("/{paymentId}")
	@ResponseStatus(HttpStatus.OK)
	public void request(@Auth AuthMember member, @PathVariable Long paymentId,
		@Valid @RequestBody PaymentRequest request) {
		paymentService.request(member.id(), paymentId, request);
	}

	@PostMapping("/confirm")
	@ResponseStatus(HttpStatus.OK)
	public void confirm(@Auth AuthMember member, @Valid @RequestBody ConfirmPaymentRequest request) {
		Payment payment = paymentService.validateInfo(member.id(), request);

		try {
			ConfirmTossPaymentResponse response = tossPaymentService.confirm(request);
			paymentService.confirm(member.id(), payment, response);
		} catch (TossPaymentException exception) {
			paymentService.fail(payment, request.paymentKey());
			throw exception;
		}
	}
}
