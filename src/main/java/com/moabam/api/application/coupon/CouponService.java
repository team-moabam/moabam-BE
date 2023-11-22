package com.moabam.api.application.coupon;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.moabam.api.domain.coupon.Coupon;
import com.moabam.api.domain.coupon.repository.CouponRepository;
import com.moabam.api.domain.coupon.repository.CouponSearchRepository;
import com.moabam.api.domain.coupon.repository.CouponWalletSearchRepository;
import com.moabam.api.domain.member.Role;
import com.moabam.api.dto.coupon.CouponResponse;
import com.moabam.api.dto.coupon.CouponStatusRequest;
import com.moabam.api.dto.coupon.CreateCouponRequest;
import com.moabam.global.auth.model.AuthMember;
import com.moabam.global.common.util.ClockHolder;
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
	private final CouponWalletSearchRepository couponWalletSearchRepository;
	private final ClockHolder clockHolder;

	@Transactional
	public void create(AuthMember admin, CreateCouponRequest request) {
		validateAdminRole(admin);
		validateConflictName(request.name());
		validatePeriod(request.startAt(), request.endAt());

		Coupon coupon = CouponMapper.toEntity(admin.id(), request);
		couponRepository.save(coupon);
	}

	@Transactional
	public void delete(AuthMember admin, Long couponId) {
		validateAdminRole(admin);
		Coupon coupon = couponRepository.findById(couponId)
			.orElseThrow(() -> new NotFoundException(ErrorMessage.NOT_FOUND_COUPON));
		couponRepository.delete(coupon);
	}

	public CouponResponse getById(Long couponId) {
		Coupon coupon = couponRepository.findById(couponId)
			.orElseThrow(() -> new NotFoundException(ErrorMessage.NOT_FOUND_COUPON));

		return CouponMapper.toDto(coupon);
	}

	public Coupon getByWallet(Long couponWalletId, Long memberId) {
		return couponWalletSearchRepository.findByIdAndMemberId(couponWalletId, memberId)
			.orElseThrow(() -> new NotFoundException(ErrorMessage.NOT_FOUND_COUPON_WALLET))
			.getCoupon();
	}

	public List<CouponResponse> getAllByStatus(CouponStatusRequest request) {
		LocalDateTime now = clockHolder.times();
		List<Coupon> coupons = couponSearchRepository.findAllByStatus(now, request);

		return coupons.stream()
			.map(CouponMapper::toDto)
			.toList();
	}

	public Coupon validatePeriod(String couponName) {
		LocalDateTime now = clockHolder.times();
		Coupon coupon = couponRepository.findByName(couponName)
			.orElseThrow(() -> new NotFoundException(ErrorMessage.NOT_FOUND_COUPON));

		if (!now.isBefore(coupon.getStartAt()) && !now.isAfter(coupon.getEndAt())) {
			return coupon;
		}

		throw new BadRequestException(ErrorMessage.INVALID_COUPON_PERIOD_END);
	}

	private void validatePeriod(LocalDateTime startAt, LocalDateTime endAt) {
		if (startAt.isAfter(endAt)) {
			throw new BadRequestException(ErrorMessage.INVALID_COUPON_PERIOD);
		}
	}

	private void validateAdminRole(AuthMember admin) {
		if (!admin.role().equals(Role.ADMIN)) {
			throw new NotFoundException(ErrorMessage.MEMBER_NOT_FOUND);
		}
	}

	private void validateConflictName(String couponName) {
		if (couponRepository.existsByName(couponName)) {
			throw new ConflictException(ErrorMessage.CONFLICT_COUPON_NAME);
		}
	}
}
