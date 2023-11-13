package com.moabam.api.application.product;

import java.util.List;

import com.moabam.api.domain.product.Product;
import com.moabam.api.dto.product.ProductResponse;
import com.moabam.api.dto.product.ProductsResponse;
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
