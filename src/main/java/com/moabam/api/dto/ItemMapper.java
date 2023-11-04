package com.moabam.api.dto;

import java.util.List;

import com.moabam.api.domain.entity.Item;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
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
			.purchasedItems(purchasedItems.stream()
				.map(ItemMapper::toItemResponse)
				.toList())
			.notPurchasedItems(notPurchasedItems.stream()
				.map(ItemMapper::toItemResponse)
				.toList())
			.build();
	}
}
