package com.moabam.api.domain.coupon.repository;

import static com.moabam.api.domain.coupon.QCouponWallet.*;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.moabam.api.domain.coupon.CouponWallet;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CouponWalletSearchRepository {

	private final JPAQueryFactory jpaQueryFactory;

	public Optional<CouponWallet> findByMemberIdAndCouponId(Long memberId, Long couponId) {
		return Optional.ofNullable(jpaQueryFactory
			.selectFrom(couponWallet)
			.where(
				couponWallet.memberId.eq(memberId),
				couponWallet.coupon.id.eq(couponId)
			)
			.fetchOne()
		);
	}
}
