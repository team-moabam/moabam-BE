package com.moabam.api.dto;

import java.util.List;

import lombok.Builder;

@Builder
public record ItemsResponse(
	List<ItemResponse> purchasedItems,
	List<ItemResponse> notPurchasedItems
) {

}
