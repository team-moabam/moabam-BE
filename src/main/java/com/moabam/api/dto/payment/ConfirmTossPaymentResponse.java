package com.moabam.api.dto.payment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Builder;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record ConfirmTossPaymentResponse(
	String paymentKey,
	String orderId,
	String orderName,
	int totalAmount
) {

}
