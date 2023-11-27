package com.moabam.api.dto.bug;

import java.util.List;

import lombok.Builder;

@Builder
public record BugHistoryResponse(
	List<BugHistoryItemResponse> history
) {

}
