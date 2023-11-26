package com.moabam.api.application.coupon;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.moabam.api.domain.coupon.Coupon;
import com.moabam.api.domain.coupon.CouponWallet;
import com.moabam.api.domain.coupon.repository.CouponManageRepository;
import com.moabam.api.domain.coupon.repository.CouponRepository;
import com.moabam.api.domain.coupon.repository.CouponWalletRepository;
import com.moabam.global.auth.model.AuthMember;
import com.moabam.global.common.util.ClockHolder;
import com.moabam.global.error.exception.BadRequestException;
import com.moabam.global.error.model.ErrorMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponManageService {

	private static final long ISSUE_SIZE = 10;

	private final ClockHolder clockHolder;

	private final CouponRepository couponRepository;
	private final CouponManageRepository couponManageRepository;
	private final CouponWalletRepository couponWalletRepository;

	@Scheduled(fixedDelay = 1000)
	public void issue() {
		LocalDate now = LocalDate.from(clockHolder.times());
		Optional<Coupon> isCoupon = couponRepository.findByStartAt(now);

		if (!canIssue(isCoupon)) {
			return;
		}

		Coupon coupon = isCoupon.get();
		Set<Long> membersId = couponManageRepository.popMinQueue(coupon.getName(), ISSUE_SIZE);

		membersId.forEach(memberId -> {
			int nextStock = couponManageRepository.increaseIssuedStock(coupon.getName());

			if (coupon.getStock() < nextStock) {
				return;
			}

			CouponWallet couponWallet = CouponWallet.create(memberId, coupon);
			couponWalletRepository.save(couponWallet);
		});
	}

	public void register(AuthMember authMember, String couponName) {
		double registerTime = System.currentTimeMillis();
		validateRegister(couponName);
		couponManageRepository.addIfAbsentQueue(couponName, authMember.id(), registerTime);
	}

	public void deleteCouponManage(String couponName) {
		couponManageRepository.deleteQueue(couponName);
		couponManageRepository.deleteIssuedStock(couponName);
	}

	private void validateRegister(String couponName) {
		LocalDate now = LocalDate.from(clockHolder.times());
		Optional<Coupon> coupon = couponRepository.findByStartAt(now);

		if (coupon.isEmpty() || !coupon.get().getName().equals(couponName)) {
			throw new BadRequestException(ErrorMessage.INVALID_COUPON_PERIOD);
		}
	}

	private boolean canIssue(Optional<Coupon> coupon) {
		if (coupon.isEmpty()) {
			return false;
		}

		Coupon currentCoupon = coupon.get();
		int currentStock = couponManageRepository.getIssuedStock(currentCoupon.getName());
		int maxStock = currentCoupon.getStock();

		return currentStock < maxStock;
	}
}
