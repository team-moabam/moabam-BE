package com.moabam.api.dto;

import lombok.Builder;

@Builder
public record CouponSearchRequest(
	boolean couponOngoing,
	boolean couponNotStarted,
	boolean couponEnded
) {

}
