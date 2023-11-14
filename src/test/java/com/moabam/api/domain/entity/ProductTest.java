package com.moabam.api.domain.entity;

import static org.assertj.core.api.AssertionsForClassTypes.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.moabam.api.domain.product.Product;
import com.moabam.global.error.exception.BadRequestException;

class ProductTest {

	@DisplayName("상품 가격이 0 보다 작으면 예외가 발생한다.")
	@Test
	void validate_price_exception() {
		Product.ProductBuilder productBuilder = Product.builder()
			.name("X10")
			.price(-10);

		assertThatThrownBy(productBuilder::build)
			.isInstanceOf(BadRequestException.class)
			.hasMessage("가격은 0 이상이어야 합니다.");
	}

	@DisplayName("상품량이 1 보다 작으면 예외가 발생한다.")
	@Test
	void validate_quantity_exception() {
		Product.ProductBuilder productBuilder = Product.builder()
			.name("X10")
			.price(1000)
			.quantity(-1);

		assertThatThrownBy(productBuilder::build)
			.isInstanceOf(BadRequestException.class)
			.hasMessage("수량은 1 이상이어야 합니다.");
	}
}
