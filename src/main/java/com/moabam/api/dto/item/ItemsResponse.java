package com.moabam.api.dto.item;

import java.util.List;

import lombok.Builder;

@Builder
public record ItemsResponse(
	Long defaultItemId,
	List<ItemResponse> purchasedItems,
	List<ItemResponse> notPurchasedItems
) {

}
