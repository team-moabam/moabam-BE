package com.moabam.api.domain.repository;

import static com.moabam.api.domain.entity.QCoupon.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.moabam.api.domain.entity.Coupon;
import com.moabam.api.dto.CouponSearchRequest;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CouponSearchRepository {

	private final JPAQueryFactory jpaQueryFactory;

	public Optional<Coupon> findById(Long couponId) {
		return Optional.ofNullable(
			jpaQueryFactory.selectFrom(coupon)
				.where(coupon.id.eq(couponId))
				.fetchOne()
		);
	}

	public List<Coupon> findAllByStatus(LocalDateTime now, CouponSearchRequest request) {
		return jpaQueryFactory.selectFrom(coupon)
			.where(filterCouponStatus(now, request))
			.fetch();
	}

	private BooleanExpression filterCouponStatus(LocalDateTime now, CouponSearchRequest request) {
		if (request.couponOngoing() && request.couponNotStarted() && request.couponEnded()) {
			return null;
		}

		if (request.couponOngoing() && request.couponNotStarted()) {
			return coupon.endAt.goe(now);
		}

		if (request.couponOngoing() && request.couponEnded()) {
			return coupon.startAt.loe(now);
		}

		if (request.couponNotStarted() && request.couponEnded()) {
			return coupon.startAt.gt(now)
				.or(coupon.endAt.lt(now));
		}

		if (request.couponOngoing()) {
			return coupon.startAt.loe(now)
				.and(coupon.endAt.goe(now));
		}

		return Expressions.FALSE;
	}
}
