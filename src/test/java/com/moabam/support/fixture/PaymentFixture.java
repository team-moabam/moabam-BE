package com.moabam.support.fixture;

import static com.moabam.support.fixture.ProductFixture.*;

import com.moabam.api.domain.coupon.Coupon;
import com.moabam.api.domain.payment.Order;
import com.moabam.api.domain.payment.Payment;
import com.moabam.api.domain.product.Product;
import com.moabam.api.dto.payment.ConfirmPaymentRequest;
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
			.totalAmount(product.getPrice())
			.build();
	}

	public static Payment paymentWithCoupon(Product product, Coupon coupon, Long couponWalletId) {
		return Payment.builder()
			.memberId(1L)
			.product(product)
			.couponWalletId(couponWalletId)
			.order(order(product))
			.totalAmount(product.getPrice())
			.discountAmount(coupon.getPoint())
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

	public static ConfirmTossPaymentResponse confirmTossPaymentResponse() {
		return ConfirmTossPaymentResponse.builder()
			.paymentKey(PAYMENT_KEY)
			.orderId(ORDER_ID)
			.orderName(BUG_PRODUCT_NAME)
			.totalAmount(AMOUNT)
			.build();
	}
}
