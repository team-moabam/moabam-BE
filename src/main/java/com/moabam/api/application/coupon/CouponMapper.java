package com.moabam.api.application.coupon;

import java.util.List;

import com.moabam.api.domain.coupon.Coupon;
import com.moabam.api.domain.coupon.CouponType;
import com.moabam.api.domain.coupon.CouponWallet;
import com.moabam.api.dto.coupon.CouponResponse;
import com.moabam.api.dto.coupon.CreateCouponRequest;
import com.moabam.api.dto.coupon.MyCouponResponse;
import com.moabam.global.common.util.StreamUtils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CouponMapper {

	public static Coupon toEntity(Long adminId, CreateCouponRequest coupon) {
		return Coupon.builder()
			.name(coupon.name())
			.description(coupon.description())
			.type(CouponType.from(coupon.type()))
			.point(coupon.point())
			.stock(coupon.stock())
			.startAt(coupon.startAt())
			.openAt(coupon.openAt())
			.adminId(adminId)
			.build();
	}

	// TODO : Admin Table 생성 시, 관리자 명 추가할 예정
	public static CouponResponse toResponse(Coupon coupon) {
		return CouponResponse.builder()
			.id(coupon.getId())
			.adminName(coupon.getAdminId() + "admin")
			.name(coupon.getName())
			.description(coupon.getDescription())
			.point(coupon.getPoint())
			.stock(coupon.getStock())
			.type(coupon.getType())
			.startAt(coupon.getStartAt())
			.openAt(coupon.getOpenAt())
			.build();
	}

	public static List<CouponResponse> toResponses(List<Coupon> coupons) {
		return StreamUtils.map(coupons, CouponMapper::toResponse);
	}

	public static MyCouponResponse toMyResponse(CouponWallet couponWallet) {
		Coupon coupon = couponWallet.getCoupon();

		return MyCouponResponse.builder()
			.id(coupon.getId())
			.name(coupon.getName())
			.description(coupon.getDescription())
			.point(coupon.getPoint())
			.type(coupon.getType())
			.build();
	}

	public static List<MyCouponResponse> toMyResponses(List<CouponWallet> couponWallets) {
		return StreamUtils.map(couponWallets, CouponMapper::toMyResponse);
	}
}
