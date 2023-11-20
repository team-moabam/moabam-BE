package com.moabam.api.dto.bug;

import lombok.Builder;

@Builder
public record TodayBugResponse(
	int morningBug,
	int nightBug
) {

}
