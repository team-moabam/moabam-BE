package com.moabam.api.dto.product;

import java.util.List;

import lombok.Builder;

@Builder
public record ProductsResponse(
	List<ProductResponse> products
) {

}
