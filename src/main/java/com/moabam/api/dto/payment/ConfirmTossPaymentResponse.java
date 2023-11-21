package com.moabam.api.dto.payment;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.moabam.api.domain.payment.PaymentStatus;

import lombok.Builder;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record ConfirmTossPaymentResponse(
	String paymentKey,
	String orderId,
	String orderName,
	PaymentStatus status,
	Long totalAmount,
	LocalDateTime requestedAt,
	LocalDateTime approvedAt
) {

}
