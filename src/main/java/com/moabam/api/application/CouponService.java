package com.moabam.api.application;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.moabam.api.domain.entity.Coupon;
import com.moabam.api.domain.repository.CouponRepository;
import com.moabam.api.domain.repository.CouponSearchRepository;
import com.moabam.api.dto.CouponMapper;
import com.moabam.api.dto.CouponResponse;
import com.moabam.api.dto.CouponSearchRequest;
import com.moabam.api.dto.CreateCouponRequest;
import com.moabam.global.error.exception.BadRequestException;
import com.moabam.global.error.exception.ConflictException;
import com.moabam.global.error.exception.NotFoundException;
import com.moabam.global.error.model.ErrorMessage;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CouponService {

	private final CouponRepository couponRepository;
	private final CouponSearchRepository couponSearchRepository;

	@Transactional
	public void createCoupon(Long adminId, CreateCouponRequest request) {
		validateConflictCouponName(request.name());
		validateCouponPeriod(request.startAt(), request.endAt());

		Coupon coupon = CouponMapper.toEntity(adminId, request);
		couponRepository.save(coupon);
	}

	@Transactional
	public void deleteCoupon(Long adminId, Long couponId) {
		Coupon coupon = couponRepository.findById(couponId)
			.orElseThrow(() -> new NotFoundException(ErrorMessage.NOT_FOUND_COUPON));
		couponRepository.delete(coupon);
	}

	public CouponResponse getCouponById(Long couponId) {
		Coupon coupon = couponSearchRepository.findById(couponId)
			.orElseThrow(() -> new NotFoundException(ErrorMessage.NOT_FOUND_COUPON));

		return CouponMapper.toDto(coupon);
	}

	public List<CouponResponse> getCoupons(CouponSearchRequest request) {
		LocalDateTime now = LocalDateTime.now();
		List<Coupon> coupons = couponSearchRepository.findAllByStatus(now, request);

		return coupons.stream()
			.map(CouponMapper::toDto)
			.toList();
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
