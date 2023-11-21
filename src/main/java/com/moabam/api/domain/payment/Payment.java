package com.moabam.api.domain.payment;

import static com.moabam.global.error.model.ErrorMessage.*;
import static java.lang.Math.*;
import static java.util.Objects.*;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.moabam.api.domain.coupon.Coupon;
import com.moabam.api.domain.product.Product;
import com.moabam.global.error.exception.BadRequestException;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "payment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Payment {

	private static final int MIN_AMOUNT = 0;

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

	@Column(name = "amount", nullable = false)
	private int amount;

	@Column(name = "payment_key")
	private String paymentKey;

	@Enumerated(value = EnumType.STRING)
	@Column(name = "status", nullable = false)
	private PaymentStatus status;

	@CreatedDate
	@Column(name = "created_at", updatable = false, nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "requested_at")
	private LocalDateTime requestedAt;

	@Column(name = "approved_at")
	private LocalDateTime approvedAt;

	@Builder
	public Payment(Long memberId, Product product, Order order, int amount, PaymentStatus status) {
		this.memberId = requireNonNull(memberId);
		this.product = requireNonNull(product);
		this.order = requireNonNull(order);
		this.amount = validateAmount(amount);
		this.status = requireNonNullElse(status, PaymentStatus.PENDING);
	}

	private int validateAmount(int amount) {
		if (amount < MIN_AMOUNT) {
			throw new BadRequestException(INVALID_PAYMENT_AMOUNT);
		}

		return amount;
	}

	public void validateByMember(Long memberId) {
		if (!this.memberId.equals(memberId)) {
			throw new BadRequestException(INVALID_MEMBER_PAYMENT);
		}
	}

	public void applyCoupon(Coupon coupon) {
		this.coupon = coupon;
		this.amount = max(MIN_AMOUNT, amount - coupon.getPoint());
	}

	public void request(String orderId) {
		this.order.updateId(orderId);
		this.status = PaymentStatus.REQUEST;
	}
}
