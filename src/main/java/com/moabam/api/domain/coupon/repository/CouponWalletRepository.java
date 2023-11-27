package com.moabam.api.domain.coupon.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moabam.api.domain.coupon.CouponWallet;

public interface CouponWalletRepository extends JpaRepository<CouponWallet, Long> {

	Optional<CouponWallet> findByIdAndMemberId(Long id, Long memberId);
}
