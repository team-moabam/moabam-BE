package com.moabam.api.domain.payment;

import static com.moabam.global.error.model.ErrorMessage.*;
import static java.util.Objects.*;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.moabam.api.domain.coupon.CouponWallet;
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

	@Column(name = "coupon_wallet_id")
	private Long couponWalletId;

	@Embedded
	private Order order;

	@Column(name = "total_amount", nullable = false)
	private int totalAmount;

	@Column(name = "discount_amount", nullable = false)
	private int discountAmount;

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
	public Payment(Long memberId, Product product, Long couponWalletId, Order order, int totalAmount,
		int discountAmount, PaymentStatus status) {
		this.memberId = requireNonNull(memberId);
		this.product = requireNonNull(product);
		this.couponWalletId = couponWalletId;
		this.order = requireNonNull(order);
		this.totalAmount = validateAmount(totalAmount);
		this.discountAmount = validateAmount(discountAmount);
		this.status = requireNonNullElse(status, PaymentStatus.READY);
	}

	private int validateAmount(int amount) {
		if (amount < MIN_AMOUNT) {
			throw new BadRequestException(INVALID_PAYMENT_AMOUNT);
		}

		return amount;
	}

	public void validateInfo(Long memberId, int amount) {
		validateByMember(memberId);
		validateByTotalAmount(amount);
	}

	public void validateByMember(Long memberId) {
		if (!this.memberId.equals(memberId)) {
			throw new BadRequestException(INVALID_MEMBER_PAYMENT);
		}
	}

	private void validateByTotalAmount(int amount) {
		if (this.totalAmount != amount) {
			throw new BadRequestException(INVALID_PAYMENT_INFO);
		}
	}

	public boolean isCouponApplied() {
		return !isNull(this.couponWalletId);
	}

	public void applyCoupon(CouponWallet couponWallet) {
		this.couponWalletId = couponWallet.getId();
		this.discountAmount = couponWallet.getCoupon().getPoint();
		this.totalAmount = Math.max(MIN_AMOUNT, this.totalAmount - this.discountAmount);
	}

	public void request(String orderId) {
		this.order.updateId(orderId);
		this.requestedAt = LocalDateTime.now();
	}

	public void confirm(String paymentKey, LocalDateTime approvedAt) {
		this.paymentKey = paymentKey;
		this.approvedAt = approvedAt;
		this.status = PaymentStatus.DONE;
	}

	public void fail(String paymentKey) {
		this.paymentKey = paymentKey;
		this.status = PaymentStatus.ABORTED;
	}
}
