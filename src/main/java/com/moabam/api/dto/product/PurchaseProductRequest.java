package com.moabam.api.dto.product;

import javax.annotation.Nullable;

public record PurchaseProductRequest(
	@Nullable Long couponWalletId
) {

}
