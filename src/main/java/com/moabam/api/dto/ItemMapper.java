package com.moabam.api.dto;

import java.util.List;

import com.moabam.api.domain.entity.Item;
import com.moabam.global.common.util.StreamUtils;

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
			.purchasedItems(StreamUtils.map(purchasedItems, ItemMapper::toItemResponse))
			.notPurchasedItems(StreamUtils.map(notPurchasedItems, ItemMapper::toItemResponse))
			.build();
	}
}
