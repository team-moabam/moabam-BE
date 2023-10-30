package com.moabam.api.dto;

import lombok.Builder;

@Builder
public record BugResponse(
	int morningBug,
	int nightBug,
	int goldenBug
) {

}
