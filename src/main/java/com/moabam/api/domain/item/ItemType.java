package com.moabam.api.domain.item;

import java.util.List;

import com.moabam.api.domain.bug.BugType;

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
