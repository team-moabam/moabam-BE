package com.moabam.api.domain.payment;

import static com.moabam.global.error.model.ErrorMessage.*;
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

	@Column(name = "order_id", nullable = false, unique = true)
	private String id;

	@Column(name = "order_name", nullable = false)
	private String name;

	@Column(name = "amount", nullable = false)
	private int amount;

	@Builder
	private Order(String id, String name, int amount) {
		this.id = requireNonNull(id);
		this.name = requireNonNull(name);
		this.amount = validateAmount(amount);
	}

	private int validateAmount(int amount) {
		if (amount < 0) {
			throw new BadRequestException(INVALID_QUANTITY);
		}

		return amount;
	}
}
