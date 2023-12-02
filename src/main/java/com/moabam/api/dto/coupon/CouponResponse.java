package com.moabam.api.dto.coupon;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.moabam.api.domain.coupon.CouponType;

import lombok.Builder;

@Builder
public record CouponResponse(
	Long id,
	Long adminId,
	String name,
	String description,
	int point,
	int maxCount,
	CouponType type,
	@JsonFormat(pattern = "yyyy-MM-dd")
	LocalDate startAt,
	@JsonFormat(pattern = "yyyy-MM-dd")
	LocalDate openAt
) {

}
