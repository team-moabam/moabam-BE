package com.moabam.api.domain.coupon.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moabam.api.domain.coupon.CouponWallet;

public interface CouponWalletRepository extends JpaRepository<CouponWallet, Long> {

}
