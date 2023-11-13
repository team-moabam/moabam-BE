package com.moabam.api.application.coupon;

import com.moabam.api.domain.coupon.Coupon;
import com.moabam.api.domain.coupon.CouponType;
import com.moabam.api.dto.coupon.CouponResponse;
import com.moabam.api.dto.coupon.CreateCouponRequest;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CouponMapper {

	public static Coupon toEntity(Long adminId, CreateCouponRequest request) {
		return Coupon.builder()
			.name(request.name())
			.description(request.description())
			.couponType(CouponType.from(request.couponType()))
			.point(request.point())
			.stock(request.stock())
			.startAt(request.startAt())
			.endAt(request.endAt())
			.adminId(adminId)
			.build();
	}

	// TODO : Admin Table 생성 시, 관리자 명 추가할 예정
	public static CouponResponse toDto(Coupon coupon) {
		return CouponResponse.builder()
			.couponId(coupon.getId())
			.couponAdminName(coupon.getAdminId() + "admin")
			.name(coupon.getName())
			.description(coupon.getDescription())
			.point(coupon.getPoint())
			.stock(coupon.getStock())
			.couponType(coupon.getCouponType())
			.startAt(coupon.getStartAt())
			.endAt(coupon.getEndAt())
			.build();
	}
}
