package com.moabam.api.presentation;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.moabam.api.application.coupon.CouponService;
import com.moabam.api.dto.coupon.CouponResponse;
import com.moabam.api.dto.coupon.CouponSearchRequest;
import com.moabam.api.dto.coupon.CreateCouponRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class CouponController {

	private final CouponService couponService;

	@PostMapping("/admins/coupons")
	@ResponseStatus(HttpStatus.CREATED)
	public void createCoupon(@Valid @RequestBody CreateCouponRequest request) {
		couponService.createCoupon(1L, request);
	}

	@DeleteMapping("/admins/coupons/{couponId}")
	@ResponseStatus(HttpStatus.OK)
	public void deleteCoupon(@PathVariable Long couponId) {
		couponService.deleteCoupon(1L, couponId);
	}

	@GetMapping("/coupons/{couponId}")
	@ResponseStatus(HttpStatus.OK)
	public CouponResponse getCouponById(@PathVariable Long couponId) {
		return couponService.getCouponById(couponId);
	}

	@PostMapping("/coupons/search")
	@ResponseStatus(HttpStatus.OK)
	public List<CouponResponse> getCoupons(@Valid @RequestBody CouponSearchRequest request) {
		return couponService.getCoupons(request);
	}
}
