package com.moabam.api.domain.coupon.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moabam.api.domain.coupon.Coupon;

public interface CouponRepository extends JpaRepository<Coupon, Long> {

	boolean existsByName(String couponName);

	boolean existsByStartAt(LocalDate startAt);

	Optional<Coupon> findByStartAt(LocalDate startAt);

	Optional<Coupon> findByNameAndStartAt(String couponName, LocalDate startAt);
}
