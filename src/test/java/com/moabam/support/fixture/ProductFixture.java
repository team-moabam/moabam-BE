package com.moabam.support.fixture;

import com.moabam.api.domain.product.Product;
import com.moabam.api.domain.product.ProductType;

public class ProductFixture {

	public static final String BUG_PRODUCT_NAME = "황금벌레 10";
	public static final int BUG_PRODUCT_PRICE = 3000;
	public static final int BUG_PRODUCT_QUANTITY = 10;

	public static Product bugProduct() {
		return Product.builder()
			.type(ProductType.BUG)
			.name(BUG_PRODUCT_NAME)
			.price(BUG_PRODUCT_PRICE)
			.quantity(BUG_PRODUCT_QUANTITY)
			.build();
	}
}
