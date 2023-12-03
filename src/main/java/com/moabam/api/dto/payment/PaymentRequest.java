package com.moabam.api.dto.payment;

import jakarta.validation.constraints.NotBlank;

public record PaymentRequest(
	@NotBlank String orderId
) {

}
