package com.moabam.api.domain.coupon.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moabam.api.domain.coupon.Coupon;

public interface CouponRepository extends JpaRepository<Coupon, Long> {

	Optional<Coupon> findByName(String couponName);

	boolean existsByName(String name);
}
