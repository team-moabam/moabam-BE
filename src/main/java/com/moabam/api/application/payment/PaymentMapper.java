package com.moabam.api.application.payment;

import com.moabam.api.domain.payment.Order;
import com.moabam.api.domain.payment.Payment;
import com.moabam.api.domain.product.Product;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PaymentMapper {

	public static Payment toEntity(Long memberId, Product product) {
		Order order = Order.builder()
			.name(product.getName())
			.amount(product.getPrice())
			.build();

		return Payment.builder()
			.memberId(memberId)
			.product(product)
			.order(order)
			.build();
	}
}
