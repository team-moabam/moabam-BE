package com.moabam.api.presentation;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.moabam.api.application.CouponService;
import com.moabam.api.dto.CreateCouponRequest;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admins/coupons")
public class CouponController {

	private final CouponService couponService;

	@PostMapping
	public void createCoupon(@RequestBody CreateCouponRequest request) {
		couponService.createCoupon(1L, request);
	}
}
