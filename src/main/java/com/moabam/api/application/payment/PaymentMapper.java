package com.moabam.api.application.payment;

import java.util.Optional;

import com.moabam.api.domain.payment.Order;
import com.moabam.api.domain.payment.Payment;
import com.moabam.api.domain.product.Product;
import com.moabam.api.dto.payment.PaymentResponse;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PaymentMapper {

	public static Payment toPayment(Long memberId, Product product) {
		Order order = Order.builder()
			.name(product.getName())
			.build();

		return Payment.builder()
			.memberId(memberId)
			.product(product)
			.order(order)
			.totalAmount(product.getPrice())
			.build();
	}

	public static PaymentResponse toPaymentResponse(Payment payment) {
		return Optional.ofNullable(payment)
			.map(p -> PaymentResponse.builder()
				.id(p.getId())
				.orderName(p.getOrder().getName())
				.discountAmount(p.getDiscountAmount())
				.totalAmount(p.getTotalAmount())
				.build())
			.orElse(null);
	}
}
