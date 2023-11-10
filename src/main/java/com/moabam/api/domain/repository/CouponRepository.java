package com.moabam.api.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moabam.api.domain.entity.Coupon;

public interface CouponRepository extends JpaRepository<Coupon, Long> {

	boolean existsByName(String name);
}
