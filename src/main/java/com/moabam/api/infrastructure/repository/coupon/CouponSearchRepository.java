package com.moabam.api.infrastructure.repository.coupon;

import static com.moabam.api.domain.coupon.QCoupon.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.moabam.api.domain.coupon.Coupon;
import com.moabam.api.dto.coupon.CouponSearchRequest;
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

		// 시작 전이거나 진행 중인 쿠폰들을 조회하고 싶은 경우
		if (request.couponOngoing() && request.couponNotStarted()) {
			return (coupon.startAt.gt(now))
				.or(coupon.startAt.loe(now).and(coupon.endAt.goe(now)));
		}

		// 종료 됐거나 진행 중인 쿠폰들을 조회하고 싶은 경우
		if (request.couponOngoing() && request.couponEnded()) {
			return (coupon.endAt.lt(now))
				.or(coupon.startAt.loe(now).and(coupon.endAt.goe(now)));
		}

		// 진행 중이 아니고, 시작 전이거나, 종료된 쿠폰들을 조회하고 싶은 경우
		if (request.couponNotStarted() && request.couponEnded()) {
			return coupon.startAt.gt(now)
				.or(coupon.endAt.lt(now));
		}

		// 진행 중인 쿠폰들을 조회하고 싶은 경우
		if (request.couponOngoing()) {
			return coupon.startAt.loe(now)
				.and(coupon.endAt.goe(now));
		}

		// 시작 적인 쿠폰들을 조회하고 싶은 경우
		if (request.couponNotStarted()) {
			return coupon.startAt.gt(now);
		}

		// 종료된 쿠폰들을 조회하고 싶은 경우
		if (request.couponEnded()) {
			return coupon.endAt.lt(now);
		}

		return Expressions.FALSE;
	}
}
