package com.moabam.api.dto;

import lombok.Builder;

@Builder
public record ProductResponse(
	Long id,
	String type,
	String name,
	int price,
	int quantity
) {

}
