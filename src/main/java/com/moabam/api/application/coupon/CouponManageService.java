package com.moabam.api.application.coupon;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.moabam.api.application.notification.NotificationService;
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
	private static final long FIRST_INDEX = 0;
	private static final String SUCCESS_ISSUE_BODY = "%s 쿠폰 발행을 성공했습니다. 축하드립니다!";
	private static final String FAIL_ISSUE_BODY = "%s 쿠폰 발행을 실패했습니다. 다음 기회에!";

	private final ClockHolder clockHolder;
	private final NotificationService notificationService;

	private final CouponRepository couponRepository;
	private final CouponManageRepository couponManageRepository;
	private final CouponWalletRepository couponWalletRepository;

	private long start = FIRST_INDEX;
	private long end = ISSUE_SIZE;

	@Scheduled(cron = "0 0 0 * * *")
	public void init() {
		start = FIRST_INDEX;
		end = ISSUE_SIZE;
	}

	@Scheduled(fixedDelay = 1000)
	public void issue() {
		LocalDate now = clockHolder.date();
		Optional<Coupon> optionalCoupon = couponRepository.findByStartAt(now);

		if (optionalCoupon.isEmpty()) {
			return;
		}

		Coupon coupon = optionalCoupon.get();
		String couponName = coupon.getName();
		Set<Long> membersId = couponManageRepository.range(couponName, start, end);

		for (Long memberId : membersId) {
			int nextStock = couponManageRepository.increaseIssuedStock(coupon.getName());

			if (coupon.getStock() < nextStock) {
				notificationService.sendCouponIssueResult(memberId, coupon.getName(), FAIL_ISSUE_BODY);
				continue;
			}

			CouponWallet couponWallet = CouponWallet.create(memberId, coupon);
			couponWalletRepository.save(couponWallet);
			notificationService.sendCouponIssueResult(memberId, coupon.getName(), SUCCESS_ISSUE_BODY);
		}

		start = end;
		end = Math.min(couponManageRepository.queueSize(couponName), end + ISSUE_SIZE);
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
		LocalDate now = clockHolder.date();
		Optional<Coupon> coupon = couponRepository.findByStartAt(now);

		if (coupon.isEmpty() || !coupon.get().getName().equals(couponName)) {
			throw new BadRequestException(ErrorMessage.INVALID_COUPON_PERIOD);
		}
	}
}
