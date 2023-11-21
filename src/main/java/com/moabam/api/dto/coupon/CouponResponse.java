package com.moabam.api.dto.coupon;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.moabam.api.domain.coupon.CouponType;

import lombok.Builder;

@Builder
public record CouponResponse(
	Long id,
	String adminName,
	String name,
	String description,
	int point,
	int stock,
	CouponType type,
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
	LocalDateTime startAt,
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
	LocalDateTime endAt
) {

}
