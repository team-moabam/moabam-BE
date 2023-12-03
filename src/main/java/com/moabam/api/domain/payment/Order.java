package com.moabam.api.domain.payment;

import static java.util.Objects.*;

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

	@Column(name = "order_id")
	private String id;

	@Column(name = "order_name", nullable = false)
	private String name;

	@Builder
	private Order(String name) {
		this.name = requireNonNull(name);
	}

	public void updateId(String id) {
		this.id = id;
	}
}
