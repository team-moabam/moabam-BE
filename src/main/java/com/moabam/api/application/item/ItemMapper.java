package com.moabam.api.application.item;

import java.util.List;

import com.moabam.api.domain.item.Inventory;
import com.moabam.api.domain.item.Item;
import com.moabam.api.dto.item.ItemResponse;
import com.moabam.api.dto.item.ItemsResponse;
import com.moabam.global.common.util.StreamUtils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ItemMapper {

	public static ItemResponse toItemResponse(Item item) {
		return ItemResponse.builder()
			.id(item.getId())
			.type(item.getType().name())
			.category(item.getCategory().name())
			.name(item.getName())
			.image(item.getImage())
			.level(item.getUnlockLevel())
			.bugPrice(item.getBugPrice())
			.goldenBugPrice(item.getGoldenBugPrice())
			.build();
	}

	public static ItemsResponse toItemsResponse(List<Item> purchasedItems, List<Item> notPurchasedItems) {
		return ItemsResponse.builder()
			.purchasedItems(StreamUtils.map(purchasedItems, ItemMapper::toItemResponse))
			.notPurchasedItems(StreamUtils.map(notPurchasedItems, ItemMapper::toItemResponse))
			.build();
	}

	public static Inventory toInventory(Long memberId, Item item) {
		return Inventory.builder()
			.memberId(memberId)
			.item(item)
			.build();
	}
}
