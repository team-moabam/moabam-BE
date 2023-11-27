package com.moabam.api.infrastructure.payment;

import com.moabam.api.dto.payment.ConfirmTossPaymentRequest;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TossPaymentMapper {

	public static ConfirmTossPaymentRequest toConfirmRequest(String paymentKey, String orderId, int amount) {
		return ConfirmTossPaymentRequest.builder()
			.paymentKey(paymentKey)
			.orderId(orderId)
			.amount(amount)
			.build();
	}
}
