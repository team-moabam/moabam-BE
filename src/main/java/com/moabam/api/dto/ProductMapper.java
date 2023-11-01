package com.moabam.api.dto;

import java.util.List;

import com.moabam.api.domain.entity.Product;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
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
			.products(products.stream()
				.map(ProductMapper::toProductResponse)
				.toList())
			.build();
	}
}
