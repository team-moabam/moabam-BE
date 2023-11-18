package com.moabam.api.domain.payment;

import static com.moabam.global.error.model.ErrorMessage.*;
import static java.util.Objects.*;

import com.moabam.api.domain.coupon.Coupon;
import com.moabam.api.domain.product.Product;
import com.moabam.global.common.entity.BaseTimeEntity;
import com.moabam.global.error.exception.BadRequestException;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "payment", indexes = @Index(name = "idx_order_id", columnList = "order_id"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "member_id", updatable = false, nullable = false)
	private Long memberId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "product_id", updatable = false, nullable = false)
	private Product product;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "coupon_id")
	private Coupon coupon;

	@Embedded
	private Order order;

	@Column(name = "payment_key")
	private String paymentKey;

	@Enumerated(value = EnumType.STRING)
	@Column(name = "status", nullable = false)
	private PaymentStatus status;

	@Builder
	public Payment(Long memberId, Product product, Coupon coupon, Order order, String paymentKey,
		PaymentStatus status) {
		this.memberId = requireNonNull(memberId);
		this.product = requireNonNull(product);
		this.coupon = coupon;
		this.order = requireNonNull(order);
		this.paymentKey = paymentKey;
		this.status = requireNonNullElse(status, PaymentStatus.PENDING);
	}

	public void applyCoupon(Coupon coupon) {
		this.order.discountAmount(coupon.getPoint());
		this.coupon = coupon;
	}

	public void validateByMember(Long memberId) {
		if (!this.memberId.equals(memberId)) {
			throw new BadRequestException(INVALID_MEMBER_PAYMENT);
		}
	}

	public void request(String orderId) {
		this.order.updateId(orderId);
		this.status = PaymentStatus.REQUEST;
	}
}
