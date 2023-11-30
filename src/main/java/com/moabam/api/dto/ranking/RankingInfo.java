package com.moabam.api.dto.ranking;

import lombok.Builder;

@Builder
public record RankingInfo(
	Long memberId,
	String nickname,
	String image
) {

}
