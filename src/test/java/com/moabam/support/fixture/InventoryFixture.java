package com.moabam.support.fixture;

import com.moabam.api.domain.entity.Inventory;
import com.moabam.api.domain.entity.Item;

public class InventoryFixture {

	public static Inventory inventory(Long memberId, Item item) {
		return Inventory.builder()
			.memberId(memberId)
			.item(item)
			.build();
	}
}
