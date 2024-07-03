package com.moabam.api.application.coupon;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.moabam.api.application.notification.NotificationService;
import com.moabam.api.domain.coupon.Coupon;
import com.moabam.api.domain.coupon.CouponWallet;
import com.moabam.api.domain.coupon.repository.CouponManageRepository;
import com.moabam.api.domain.coupon.repository.CouponWalletRepository;
import com.moabam.global.common.util.ClockHolder;
import com.moabam.global.error.exception.ConflictException;
import com.moabam.global.error.model.ErrorMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponManageService {

	private static final String SUCCESS_ISSUE_BODY = "%s 쿠폰 발행을 성공했습니다. 축하드립니다!";
	private static final String FAIL_ISSUE_BODY = "%s 쿠폰 발행을 실패했습니다. 다음 기회에!";
	private static final long ISSUE_SIZE = 10;

	private final ClockHolder clockHolder;
	private final NotificationService notificationService;

	private final CouponCacheService couponCacheService;
	private final CouponManageRepository couponManageRepository;
	private final CouponWalletRepository couponWalletRepository;

	@Scheduled(fixedDelay = 1000)
	public void issue() {
		LocalDate now = clockHolder.date();
		Optional<Coupon> optionalCoupon = couponCacheService.getByStartAt(now);

		if (optionalCoupon.isEmpty()) {
			return;
		}

		Coupon coupon = optionalCoupon.get();
		String couponName = coupon.getName();
		int maxCount = coupon.getMaxCount();
		int currentCount = couponManageRepository.getCount(couponName);
		Set<Long> membersId = couponManageRepository.rangeQueue(couponName, currentCount, currentCount + ISSUE_SIZE);

		if (membersId == null || membersId.isEmpty()) {
			return;
		}

		for (Long memberId : membersId) {
			int rank = couponManageRepository.rankQueue(couponName, memberId);

			if (maxCount <= rank) {
				notificationService.sendCouponIssueResult(memberId, couponName, FAIL_ISSUE_BODY);
				continue;
			}

			couponWalletRepository.save(CouponWallet.create(memberId, coupon));
			notificationService.sendCouponIssueResult(memberId, couponName, SUCCESS_ISSUE_BODY);
		}

		couponManageRepository.increase(couponName, membersId.size());
	}

	public void delete(String couponName) {
		couponManageRepository.deleteQueue(couponName);
		couponManageRepository.deleteCount(couponName);
	}

	public void registerQueue(String couponName, Long memberId) {
		double registerTime = System.currentTimeMillis();
		validateRegisterQueue(couponName, memberId);
		couponManageRepository.addIfAbsentQueue(couponName, memberId, registerTime);
	}

	private void validateRegisterQueue(String couponName, Long memberId) {
		LocalDate now = clockHolder.date();
		couponCacheService.getByNameAndStartAt(couponName, now);

		if (couponManageRepository.hasValue(couponName, memberId)) {
			throw new ConflictException(ErrorMessage.CONFLICT_COUPON_ISSUE);
		}

		notificationService.sendCouponIssueResult(memberId, couponName, FAIL_ISSUE_BODY);
	}
}
