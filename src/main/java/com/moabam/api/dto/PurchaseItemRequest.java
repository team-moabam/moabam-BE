package com.moabam.api.dto;

import com.moabam.api.domain.entity.enums.BugType;

import jakarta.validation.constraints.NotNull;

public record PurchaseItemRequest(
	@NotNull BugType bugType
) {

}
