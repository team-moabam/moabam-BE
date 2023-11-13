package com.moabam.api.domain.product;

import static com.moabam.global.error.model.ErrorMessage.*;
import static java.util.Objects.*;

import org.hibernate.annotations.ColumnDefault;

import com.moabam.global.common.entity.BaseTimeEntity;
import com.moabam.global.error.exception.BadRequestException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "product")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Enumerated(value = EnumType.STRING)
	@Column(name = "type", nullable = false)
	@ColumnDefault("'BUG'")
	private ProductType type;

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "price", nullable = false)
	private int price;

	@Column(name = "quantity", nullable = false)
	@ColumnDefault("1")
	private int quantity;

	@Builder
	private Product(ProductType type, String name, int price, Integer quantity) {
		this.type = requireNonNullElse(type, ProductType.BUG);
		this.name = requireNonNull(name);
		this.price = validatePrice(price);
		this.quantity = validateQuantity(requireNonNullElse(quantity, 1));
	}

	private int validatePrice(int price) {
		if (price < 0) {
			throw new BadRequestException(INVALID_PRICE);
		}

		return price;
	}

	private int validateQuantity(int quantity) {
		if (quantity < 1) {
			throw new BadRequestException(INVALID_QUANTITY);
		}

		return quantity;
	}
}
