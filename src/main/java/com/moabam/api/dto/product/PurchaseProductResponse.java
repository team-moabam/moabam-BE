package com.moabam.api.dto.product;

import lombok.Builder;

@Builder
public record PurchaseProductResponse(
	Long paymentId,
	String orderName,
	int price
) {

}
