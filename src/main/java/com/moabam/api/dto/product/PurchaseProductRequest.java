package com.moabam.api.dto.product;

import jakarta.annotation.Nullable;

public record PurchaseProductRequest(
	@Nullable Long couponWalletId
) {

}
