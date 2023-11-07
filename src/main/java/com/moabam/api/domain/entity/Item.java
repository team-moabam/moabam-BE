package com.moabam.api.domain.entity;

import static com.moabam.global.error.model.ErrorMessage.*;
import static java.util.Objects.*;

import org.hibernate.annotations.ColumnDefault;

import com.moabam.api.domain.entity.enums.BugType;
import com.moabam.api.domain.entity.enums.ItemCategory;
import com.moabam.api.domain.entity.enums.ItemType;
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
@Table(name = "item")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Item extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Enumerated(value = EnumType.STRING)
	@Column(name = "type", nullable = false)
	private ItemType type;

	@Enumerated(value = EnumType.STRING)
	@Column(name = "category", nullable = false)
	private ItemCategory category;

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "image", nullable = false)
	private String image;

	@Column(name = "bug_price", nullable = false)
	@ColumnDefault("0")
	private int bugPrice;

	@Column(name = "golden_bug_price", nullable = false)
	@ColumnDefault("0")
	private int goldenBugPrice;

	@Column(name = "unlock_level", nullable = false)
	@ColumnDefault("1")
	private int unlockLevel;

	@Builder
	private Item(ItemType type, ItemCategory category, String name, String image, int bugPrice, int goldenBugPrice,
		Integer unlockLevel) {
		this.type = requireNonNull(type);
		this.category = requireNonNull(category);
		this.name = requireNonNull(name);
		this.image = requireNonNull(image);
		this.bugPrice = validatePrice(bugPrice);
		this.goldenBugPrice = validatePrice(goldenBugPrice);
		this.unlockLevel = validateLevel(requireNonNullElse(unlockLevel, 1));
	}

	private int validatePrice(int price) {
		if (price < 0) {
			throw new BadRequestException(INVALID_PRICE);
		}

		return price;
	}

	private int validateLevel(int level) {
		if (level < 1) {
			throw new BadRequestException(INVALID_LEVEL);
		}

		return level;
	}

	public void validatePurchasable(BugType bugType, int memberLevel) {
		validateUnlocked(memberLevel);
		validatePurchasableByBugType(bugType);
	}

	private void validateUnlocked(int memberLevel) {
		if (this.unlockLevel > memberLevel) {
			throw new BadRequestException(ITEM_UNLOCK_LEVEL_HIGH);
		}
	}

	private void validatePurchasableByBugType(BugType bugType) {
		if (!this.type.isPurchasableBy(bugType)) {
			throw new BadRequestException(ITEM_NOT_PURCHASABLE_BY_BUG_TYPE);
		}
	}

	public int getPrice(BugType bugType) {
		return bugType.isGoldenBug() ? this.goldenBugPrice : this.bugPrice;
	}
}
