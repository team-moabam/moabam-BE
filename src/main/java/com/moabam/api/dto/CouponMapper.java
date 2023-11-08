package com.moabam.api.dto;

import com.moabam.api.domain.entity.Coupon;
import com.moabam.api.domain.entity.enums.CouponType;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CouponMapper {

	public static Coupon toEntity(Long adminId, CreateCouponRequest request) {
		return Coupon.builder()
			.name(request.name())
			.description(request.description())
			.type(CouponType.from(request.type()))
			.point(request.point())
			.stock(request.stock())
			.startAt(request.startAt())
			.endAt(request.endAt())
			.adminId(adminId)
			.build();
	}
}
