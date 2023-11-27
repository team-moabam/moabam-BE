package com.moabam.api.domain.payment.repository;

import static com.moabam.api.domain.payment.QPayment.*;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.moabam.api.domain.payment.Payment;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class PaymentSearchRepository {

	private final JPAQueryFactory jpaQueryFactory;

	public Optional<Payment> findByOrderId(String orderId) {
		return Optional.ofNullable(jpaQueryFactory
			.selectFrom(payment)
			.where(payment.order.id.eq(orderId))
			.fetchOne()
		);
	}
}
