package com.moabam.api.domain.coupon.repository;

import static com.moabam.api.domain.coupon.QCoupon.*;
import static com.moabam.api.domain.coupon.QCouponWallet.*;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.moabam.api.domain.coupon.CouponWallet;
import com.moabam.global.common.util.DynamicQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CouponWalletSearchRepository {

	private final JPAQueryFactory jpaQueryFactory;

	public List<CouponWallet> findAllByCouponIdAndMemberId(Long couponId, Long memberId) {
		return jpaQueryFactory
			.selectFrom(couponWallet)
			.join(couponWallet.coupon, coupon).fetchJoin()
			.where(
				DynamicQuery.generateEq(couponId, couponWallet.coupon.id::eq),
				DynamicQuery.generateEq(memberId, couponWallet.memberId::eq)
			)
			.fetch();
	}

	public Optional<CouponWallet> findByIdAndMemberId(Long id, Long memberId) {
		return Optional.ofNullable(jpaQueryFactory
			.selectFrom(couponWallet)
			.join(couponWallet.coupon, coupon).fetchJoin()
			.where(
				couponWallet.id.eq(id),
				couponWallet.memberId.eq(memberId))
			.fetchOne()
		);
	}

	public Optional<CouponWallet> findByMemberIdAndCouponId(Long memberId, Long couponId) {
		return Optional.ofNullable(jpaQueryFactory
			.selectFrom(couponWallet)
			.join(couponWallet.coupon, coupon).fetchJoin()
			.where(
				couponWallet.coupon.id.eq(couponId),
				couponWallet.memberId.eq(memberId))
			.fetchOne());
	}
}
