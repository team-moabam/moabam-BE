package com.moabam.api.dto;

public record CouponSearchRequest(
	boolean couponOngoing,
	boolean couponNotStarted,
	boolean couponEnded
) {

}
