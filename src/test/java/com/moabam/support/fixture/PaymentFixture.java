package com.moabam.support.fixture;

import static com.moabam.support.fixture.ProductFixture.*;

import com.moabam.api.domain.payment.Order;
import com.moabam.api.domain.payment.Payment;
import com.moabam.api.domain.product.Product;

public final class PaymentFixture {

	public static final String ORDER_ID = "random_order_id_123";

	public static Payment payment(Product product) {
		return Payment.builder()
			.memberId(1L)
			.product(product)
			.order(order())
			.build();
	}

	public static Order order() {
		return Order.builder()
			.name(BUG_PRODUCT_NAME)
			.amount(BUG_PRODUCT_PRICE)
			.build();
	}
}
