package com.moabam.api.dto.payment;

import jakarta.validation.constraints.NotNull;

public record PaymentRequest(
	@NotNull String orderId
) {

}
