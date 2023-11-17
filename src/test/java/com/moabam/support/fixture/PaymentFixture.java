package com.moabam.support.fixture;

import static com.moabam.support.fixture.ProductFixture.*;

import com.moabam.api.domain.payment.Order;
import com.moabam.api.domain.payment.Payment;

public final class PaymentFixture {

	public static Payment bugProductPayment() {
		return Payment.builder()
			.memberId(1L)
			.product(bugProduct())
			.order(order())
			.build();
	}

	private static Order order() {
		return Order.builder()
			.name(BUG_PRODUCT_NAME)
			.amount(BUG_PRODUCT_PRICE)
			.build();
	}
}
