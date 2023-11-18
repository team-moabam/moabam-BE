package com.moabam.api.dto.product;

import lombok.Builder;

@Builder
public record PurchaseProductResponse(
	String orderName,
	int price
) {

}
