package com.moabam.api.dto.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ConfirmPaymentRequest(
	@NotBlank String paymentKey,
	@NotBlank String orderId,
	@NotNull int amount
) {

}
