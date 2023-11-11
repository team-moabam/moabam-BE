package com.moabam.api.presentation;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.moabam.api.application.CouponService;
import com.moabam.api.dto.CreateCouponRequest;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admins")
public class CouponController {

	private final CouponService couponService;

	@PostMapping("/coupons")
	@ResponseStatus(HttpStatus.CREATED)
	public void createCoupon(@RequestBody CreateCouponRequest request) {
		couponService.createCoupon(1L, request);
	}

	@DeleteMapping("/coupons/{couponId}")
	@ResponseStatus(HttpStatus.OK)
	public void deleteCoupon(@PathVariable Long couponId) {
		couponService.deleteCoupon(1L, couponId);
	}
}
