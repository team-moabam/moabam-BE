package com.moabam.api.dto.payment;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record ConfirmPaymentRequest(
	@NotBlank String paymentKey,
	@NotBlank String orderId,
	@NotNull @Min(0) int amount
) {

}
