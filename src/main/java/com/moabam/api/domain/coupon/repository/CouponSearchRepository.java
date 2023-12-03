package com.moabam.api.domain.coupon.repository;

import static com.moabam.api.domain.coupon.QCoupon.*;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.moabam.api.domain.coupon.Coupon;
import com.moabam.api.dto.coupon.CouponStatusRequest;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CouponSearchRepository {

	private final JPAQueryFactory jpaQueryFactory;

	public List<Coupon> findAllByStatus(LocalDate now, CouponStatusRequest couponStatus) {
		return jpaQueryFactory.selectFrom(coupon)
			.where(filterStatus(now, couponStatus))
			.orderBy(coupon.startAt.asc())
			.fetch();
	}

	private BooleanExpression filterStatus(LocalDate now, CouponStatusRequest couponStatus) {
		// 모든 쿠폰 (금일 발급 가능한 쿠폰 포함)
		if (couponStatus.opened() && couponStatus.ended()) {
			return null;
		}

		// 쿠폰 정보 오픈 중인 쿠폰들 (금일 발급 가능한 쿠폰 포함)
		if (couponStatus.opened()) {
			return coupon.openAt.loe(now).and(coupon.startAt.goe(now));
		}

		// 종료된 쿠폰들
		if (couponStatus.ended()) {
			return coupon.startAt.lt(now);
		}

		// 금일 발급 가능한 쿠폰
		return coupon.startAt.eq(now);
	}
}
