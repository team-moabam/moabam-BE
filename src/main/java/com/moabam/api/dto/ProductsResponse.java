package com.moabam.api.dto;

import java.util.List;

import lombok.Builder;

@Builder
public record ProductsResponse(
	List<ProductResponse> products
) {

}
