package com.moabam.api.application.coupon;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.moabam.api.domain.coupon.Coupon;
import com.moabam.api.domain.member.Role;
import com.moabam.api.dto.coupon.CouponResponse;
import com.moabam.api.dto.coupon.CouponSearchRequest;
import com.moabam.api.dto.coupon.CreateCouponRequest;
import com.moabam.api.infrastructure.repository.coupon.CouponRepository;
import com.moabam.api.infrastructure.repository.coupon.CouponSearchRepository;
import com.moabam.global.auth.model.AuthorizationMember;
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
	public void createCoupon(AuthorizationMember admin, CreateCouponRequest request) {
		validateAdminRole(admin);
		validateConflictCouponName(request.name());
		validateCouponPeriod(request.startAt(), request.endAt());

		Coupon coupon = CouponMapper.toEntity(admin.id(), request);
		couponRepository.save(coupon);
	}

	@Transactional
	public void deleteCoupon(AuthorizationMember admin, Long couponId) {
		validateAdminRole(admin);
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

	private void validateAdminRole(AuthorizationMember admin) {
		if (!admin.role().equals(Role.ADMIN)) {
			throw new NotFoundException(ErrorMessage.MEMBER_NOT_FOUND);
		}
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
