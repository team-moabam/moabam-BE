package com.moabam.api.domain.coupon.repository;

import static com.moabam.api.domain.coupon.QCoupon.*;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.moabam.api.domain.coupon.Coupon;
import com.moabam.api.dto.coupon.CouponStatusRequest;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CouponSearchRepository {

	private final JPAQueryFactory jpaQueryFactory;

	public List<Coupon> findAllByStatus(LocalDateTime now, CouponStatusRequest couponStatus) {
		return jpaQueryFactory.selectFrom(coupon)
			.where(filterStatus(now, couponStatus))
			.fetch();
	}

	private BooleanExpression filterStatus(LocalDateTime now, CouponStatusRequest couponStatus) {
		if (couponStatus.ongoing() && couponStatus.notStarted() && couponStatus.ended()) {
			return null;
		}

		// 시작 전이거나 진행 중인 쿠폰들을 조회하고 싶은 경우
		if (couponStatus.ongoing() && couponStatus.notStarted()) {
			return (coupon.startAt.gt(now))
				.or(coupon.startAt.loe(now).and(coupon.endAt.goe(now)));
		}

		// 종료 됐거나 진행 중인 쿠폰들을 조회하고 싶은 경우
		if (couponStatus.ongoing() && couponStatus.ended()) {
			return (coupon.endAt.lt(now))
				.or(coupon.startAt.loe(now).and(coupon.endAt.goe(now)));
		}

		// 진행 중이 아니고, 시작 전이거나, 종료된 쿠폰들을 조회하고 싶은 경우
		if (couponStatus.notStarted() && couponStatus.ended()) {
			return coupon.startAt.gt(now)
				.or(coupon.endAt.lt(now));
		}

		// 진행 중인 쿠폰들을 조회하고 싶은 경우
		if (couponStatus.ongoing()) {
			return coupon.startAt.loe(now)
				.and(coupon.endAt.goe(now));
		}

		// 시작 적인 쿠폰들을 조회하고 싶은 경우
		if (couponStatus.notStarted()) {
			return coupon.startAt.gt(now);
		}

		// 종료된 쿠폰들을 조회하고 싶은 경우
		if (couponStatus.ended()) {
			return coupon.endAt.lt(now);
		}

		return Expressions.FALSE;
	}
}
