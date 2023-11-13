package com.moabam.api.dto.coupon;

import lombok.Builder;

@Builder
public record CouponSearchRequest(
	boolean couponOngoing,
	boolean couponNotStarted,
	boolean couponEnded
) {

}
