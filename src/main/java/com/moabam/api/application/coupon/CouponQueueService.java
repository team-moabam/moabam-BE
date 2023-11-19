package com.moabam.api.application.coupon;

import org.springframework.stereotype.Service;

import com.moabam.api.domain.coupon.Coupon;
import com.moabam.api.domain.coupon.repository.CouponQueueRepository;
import com.moabam.global.auth.model.AuthorizationMember;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponQueueService {

	private final CouponService couponService;
	private final CouponQueueRepository couponQueueRepository;

	public void register(AuthorizationMember member, String couponName) {
		long registerTime = System.currentTimeMillis();

		if (canRegister(couponName)) {
			log.info("{} 쿠폰이 모두 발급되었습니다.", couponName);
			return;
		}

		couponQueueRepository.addQueue(couponName, member.nickname(), registerTime);
	}

	private boolean canRegister(String couponName) {
		Coupon coupon = couponService.validateCouponPeriod(couponName);

		return coupon.getStock() <= couponQueueRepository.queueSize(coupon.getName());
	}
}
