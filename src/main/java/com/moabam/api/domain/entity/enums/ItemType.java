package com.moabam.api.domain.entity.enums;

import java.util.List;

public enum ItemType {

	MORNING(List.of(BugType.MORNING, BugType.GOLDEN)),
	NIGHT(List.of(BugType.NIGHT, BugType.GOLDEN));

	private final List<BugType> purchasableBugTypes;

	ItemType(List<BugType> purchasableBugTypes) {
		this.purchasableBugTypes = purchasableBugTypes;
	}

	public boolean isPurchasableBy(BugType bugType) {
		return this.purchasableBugTypes.contains(bugType);
	}
}
