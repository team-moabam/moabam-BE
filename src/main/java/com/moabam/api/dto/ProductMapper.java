package com.moabam.api.dto;

import java.util.List;

import com.moabam.api.domain.entity.Product;
import com.moabam.global.common.util.StreamUtils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProductMapper {

	public static ProductResponse toProductResponse(Product product) {
		return ProductResponse.builder()
			.id(product.getId())
			.type(product.getType().name())
			.name(product.getName())
			.price(product.getPrice())
			.quantity(product.getQuantity())
			.build();
	}

	public static ProductsResponse toProductsResponse(List<Product> products) {
		return ProductsResponse.builder()
			.products(StreamUtils.map(products, ProductMapper::toProductResponse))
			.build();
	}
}
