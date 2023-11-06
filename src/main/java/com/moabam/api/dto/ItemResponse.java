package com.moabam.api.dto;

import lombok.Builder;

@Builder
public record ItemResponse(
	Long id,
	String type,
	String category,
	String name,
	String image,
	int level,
	int bugPrice,
	int goldenBugPrice
) {

}
