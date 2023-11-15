package com.moabam.api.dto;

import lombok.Builder;

@Builder
public record TodayBugResponse(
	int morningBug,
	int nightBug
) {

}
