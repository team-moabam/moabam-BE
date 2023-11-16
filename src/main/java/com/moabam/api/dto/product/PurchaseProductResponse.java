package com.moabam.api.dto.product;

import lombok.Builder;

@Builder
public record PurchaseProductResponse(
	String orderId,
	String orderName,
	int price
) {

}
