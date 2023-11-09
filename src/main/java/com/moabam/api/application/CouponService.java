package com.moabam.api.application;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.moabam.api.domain.entity.Coupon;
import com.moabam.api.domain.repository.CouponRepository;
import com.moabam.api.dto.CouponMapper;
import com.moabam.api.dto.CreateCouponRequest;
import com.moabam.global.error.exception.BadRequestException;
import com.moabam.global.error.exception.ConflictException;
import com.moabam.global.error.model.ErrorMessage;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CouponService {

	private final CouponRepository couponRepository;

	@Transactional
	public void createCoupon(Long adminId, CreateCouponRequest request) {
		validateConflictCouponName(request.name());
		validateCouponPeriod(request.startAt(), request.endAt());

		Coupon coupon = CouponMapper.toEntity(adminId, request);
		couponRepository.save(coupon);
	}

	private void validateConflictCouponName(String name) {
		if (couponRepository.existsByName(name)) {
			throw new ConflictException(ErrorMessage.CONFLICT_COUPON_NAME);
		}
	}

	private void validateCouponPeriod(LocalDateTime startAt, LocalDateTime endAt) {
		if (startAt.isAfter(endAt)) {
			throw new BadRequestException(ErrorMessage.INVALID_COUPON_PERIOD);
		}
	}
}
