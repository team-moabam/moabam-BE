package com.moabam.api.presentation;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.moabam.api.domain.entity.enums.CouponType;

import lombok.Builder;

@Builder
public record CouponResponse(
	Long couponId,
	String couponAdminName,
	String name,
	String description,
	int point,
	int stock,
	CouponType couponType,
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
	LocalDateTime startAt,
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
	LocalDateTime endAt
) {

}
