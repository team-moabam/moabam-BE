package com.moabam.support.fixture;

import com.moabam.api.domain.payment.Order;
import com.moabam.api.domain.payment.Payment;
import com.moabam.api.domain.product.Product;

public final class PaymentFixture {

	public static final String PAYMENT_KEY = "payment_key_123";
	public static final String ORDER_ID = "random_order_id_123";
	public static final int AMOUNT = 2000;

	public static Payment payment(Product product) {
		return Payment.builder()
			.memberId(1L)
			.product(product)
			.order(order(product))
			.amount(product.getPrice())
			.build();
	}

	public static Order order(Product product) {
		return Order.builder()
			.name(product.getName())
			.build();
	}
}
