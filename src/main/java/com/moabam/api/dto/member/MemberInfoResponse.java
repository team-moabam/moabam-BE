package com.moabam.api.dto.member;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.*;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;

@Builder
public record MemberInfoResponse(
	String nickname,
	String profileImage,
	String intro,
	long level,
	long exp,
	Map<String, String> birds,
	List<BadgeResponse> badges,
	@JsonInclude(NON_NULL) Integer goldenBug,
	@JsonInclude(NON_NULL) Integer morningBug,
	@JsonInclude(NON_NULL) Integer nightBug
) {

}
