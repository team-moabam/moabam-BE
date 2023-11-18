package com.moabam.api.domain.payment;

import static com.moabam.support.fixture.PaymentFixture.*;
import static com.moabam.support.fixture.ProductFixture.*;
import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.moabam.global.error.exception.BadRequestException;

class OrderTest {

	@DisplayName("금액이 음수이면 예외가 발생한다.")
	@Test
	void validate_bug_count_exception() {
		Order.OrderBuilder orderBuilder = Order.builder()
			.name(BUG_PRODUCT_NAME)
			.amount(-1000);

		assertThatThrownBy(orderBuilder::build)
			.isInstanceOf(BadRequestException.class)
			.hasMessage("주문 금액은 0 이상이어야 합니다.");
	}

	@DisplayName("금액을 할인한다.")
	@Nested
	class Use {

		@DisplayName("성공한다.")
		@Test
		void success() {
			// given
			Order order = order();

			// when
			order.discountAmount(1000);

			// then
			assertThat(order.getAmount()).isEqualTo(BUG_PRODUCT_PRICE - 1000);
		}

		@DisplayName("할인 금액이 주문 금액보다 크면 0으로 처리한다.")
		@Test
		void discount_amount_greater_than_order_amount() {
			// given
			Order order = order();

			// when
			order.discountAmount(10000);

			// then
			assertThat(order.getAmount()).isZero();
		}
	}

	@DisplayName("주문 id를 갱신한다.")
	@Test
	void update_id_success() {
		// given
		Order order = order();

		// when
		order.updateId(ORDER_ID);

		// then
		assertThat(order.getId()).isEqualTo(ORDER_ID);
	}
}
