package com.moabam.api.domain.coupon.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moabam.api.domain.coupon.Coupon;

public interface CouponRepository extends JpaRepository<Coupon, Long> {

	boolean existsByName(String name);
}
