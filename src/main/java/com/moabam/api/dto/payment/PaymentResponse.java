package com.moabam.api.dto.payment;

import lombok.Builder;

@Builder
public record PaymentResponse(
	Long id,
	String orderName,
	int discountAmount,
	int totalAmount
) {

}
