package com.moabam.support.fixture;

import java.time.LocalDateTime;

import com.moabam.api.domain.payment.Order;
import com.moabam.api.domain.payment.Payment;
import com.moabam.api.domain.product.Product;
import com.moabam.api.dto.payment.ConfirmPaymentRequest;
import com.moabam.api.dto.payment.ConfirmTossPaymentRequest;
import com.moabam.api.dto.payment.ConfirmTossPaymentResponse;

public final class PaymentFixture {

	public static final String PAYMENT_KEY = "payment_key_123";
	public static final String ORDER_ID = "random_order_id_123";
	public static final int AMOUNT = 3000;

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

	public static ConfirmPaymentRequest confirmPaymentRequest() {
		return ConfirmPaymentRequest.builder()
			.paymentKey(PAYMENT_KEY)
			.orderId(ORDER_ID)
			.amount(AMOUNT)
			.build();
	}

	public static ConfirmTossPaymentRequest confirmTossPaymentRequest() {
		return ConfirmTossPaymentRequest.builder()
			.paymentKey(PAYMENT_KEY)
			.orderId(ORDER_ID)
			.amount(AMOUNT)
			.build();
	}

	public static ConfirmTossPaymentResponse confirmTossPaymentResponse() {
		return ConfirmTossPaymentResponse.builder()
			.approvedAt(LocalDateTime.of(2023, 1, 1, 1, 1))
			.build();
	}
}
