package com.moabam.api.dto.payment;

import lombok.Builder;

@Builder
public record ConfirmTossPaymentRequest(
	String paymentKey,
	String orderId,
	int amount
) {

}
