package com.moabam.api.dto.item;

import com.moabam.api.domain.bug.BugType;

import jakarta.validation.constraints.NotNull;

public record PurchaseItemRequest(
	@NotNull BugType bugType
) {

}
