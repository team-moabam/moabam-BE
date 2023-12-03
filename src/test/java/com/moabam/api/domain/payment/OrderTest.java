package com.moabam.api.domain.payment;

import static com.moabam.support.fixture.PaymentFixture.*;
import static com.moabam.support.fixture.ProductFixture.*;
import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OrderTest {

	@DisplayName("주문 id를 갱신한다.")
	@Test
	void update_id_success() {
		// given
		Order order = order(bugProduct());

		// when
		order.updateId(ORDER_ID);

		// then
		assertThat(order.getId()).isEqualTo(ORDER_ID);
	}
}
