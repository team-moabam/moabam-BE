package com.moabam.api.dto.payment;

import com.moabam.api.domain.payment.Payment;

import lombok.Builder;

@Builder
public record RequestConfirmPaymentResponse(
	Payment payment,
	String paymentKey
) {

}
