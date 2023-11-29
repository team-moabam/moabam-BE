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
import com.moabam.api.domain.coupon.repository.CouponRepository;
import com.moabam.api.domain.coupon.repository.CouponWalletRepository;
import com.moabam.global.common.util.ClockHolder;
import com.moabam.global.error.exception.BadRequestException;
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
	private static final long ISSUE_FIRST = 0;

	private final ClockHolder clockHolder;
	private final NotificationService notificationService;

	private final CouponRepository couponRepository;
	private final CouponManageRepository couponManageRepository;
	private final CouponWalletRepository couponWalletRepository;

	private long current = ISSUE_FIRST;

	@Scheduled(cron = "0 0 0 * * *")
	public void init() {
		current = ISSUE_FIRST;
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
		int max = coupon.getStock();

		Set<Long> membersId = couponManageRepository.rangeQueue(couponName, current, current + ISSUE_SIZE);

		for (Long memberId : membersId) {
			int rank = couponManageRepository.rankQueue(couponName, memberId);

			if (max < rank) {
				notificationService.sendCouponIssueResult(memberId, couponName, FAIL_ISSUE_BODY);
				continue;
			}

			couponWalletRepository.save(CouponWallet.create(memberId, coupon));
			notificationService.sendCouponIssueResult(memberId, couponName, SUCCESS_ISSUE_BODY);
			current++;
		}
	}

	public void registerQueue(Long memberId, String couponName) {
		double registerTime = System.currentTimeMillis();
		validateRegisterQueue(couponName);
		couponManageRepository.addIfAbsentQueue(couponName, memberId, registerTime);
	}

	public void deleteQueue(String couponName) {
		couponManageRepository.deleteQueue(couponName);
	}

	private void validateRegisterQueue(String couponName) {
		LocalDate now = clockHolder.date();

		if (!couponRepository.existsByNameAndStartAt(couponName, now)) {
			throw new BadRequestException(ErrorMessage.INVALID_COUPON_PERIOD);
		}
	}
}
