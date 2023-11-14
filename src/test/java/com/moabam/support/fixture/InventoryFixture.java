package com.moabam.support.fixture;

import com.moabam.api.domain.item.Inventory;
import com.moabam.api.domain.item.Item;

public class InventoryFixture {

	public static Inventory inventory(Long memberId, Item item) {
		return Inventory.builder()
			.memberId(memberId)
			.item(item)
			.build();
	}
}
