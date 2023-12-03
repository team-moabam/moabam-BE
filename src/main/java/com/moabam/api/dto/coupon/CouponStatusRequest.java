package com.moabam.api.dto.coupon;

import lombok.Builder;

@Builder
public record CouponStatusRequest(
	boolean opened,
	boolean ended
) {

}
