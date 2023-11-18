package com.moabam.api.domain.payment;

import static com.moabam.global.error.model.ErrorMessage.*;
import static java.lang.Math.*;
import static java.util.Objects.*;

import com.moabam.global.error.exception.BadRequestException;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

	private static final int MIN_AMOUNT = 0;

	@Column(name = "order_id")
	private String id;

	@Column(name = "order_name", nullable = false)
	private String name;

	@Column(name = "amount", nullable = false)
	private int amount;

	@Builder
	private Order(String id, String name, int amount) {
		this.id = id;
		this.name = requireNonNull(name);
		this.amount = validateAmount(amount);
	}

	private int validateAmount(int amount) {
		if (amount < MIN_AMOUNT) {
			throw new BadRequestException(INVALID_ORDER_AMOUNT);
		}

		return amount;
	}

	public void discountAmount(int price) {
		this.amount = max(MIN_AMOUNT, amount - price);
	}

	public void updateId(String id) {
		this.id = id;
	}
}
