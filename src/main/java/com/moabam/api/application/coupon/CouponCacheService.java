package com.moabam.api.application.coupon;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.moabam.api.domain.coupon.Coupon;
import com.moabam.api.domain.coupon.repository.CouponRepository;
import com.moabam.global.error.exception.NotFoundException;
import com.moabam.global.error.model.ErrorMessage;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "coupons")
public class CouponCacheService {

	private final CouponRepository couponRepository;

	@Cacheable(key = "#couponName + #now")
	public Coupon getByNameAndStartAt(String couponName, LocalDate now) {
		return couponRepository.findByNameAndStartAt(couponName, now)
			.orElseThrow(() -> new NotFoundException(ErrorMessage.INVALID_COUPON_PERIOD));
	}

	@Cacheable(key = "#now")
	public Optional<Coupon> getByStartAt(LocalDate now) {
		return couponRepository.findByStartAt(now);
	}
}
