package com.moabam.api.presentation;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.moabam.api.application.coupon.CouponQueueService;
import com.moabam.api.application.coupon.CouponService;
import com.moabam.api.dto.coupon.CouponResponse;
import com.moabam.api.dto.coupon.CouponStatusRequest;
import com.moabam.api.dto.coupon.CreateCouponRequest;
import com.moabam.global.auth.annotation.CurrentMember;
import com.moabam.global.auth.model.AuthorizationMember;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class CouponController {

	private final CouponService couponService;
	private final CouponQueueService couponQueueService;

	@PostMapping("/admins/coupons")
	@ResponseStatus(HttpStatus.CREATED)
	public void create(@CurrentMember AuthorizationMember admin,
		@Valid @RequestBody CreateCouponRequest request) {
		couponService.create(admin, request);
	}

	@DeleteMapping("/admins/coupons/{couponId}")
	@ResponseStatus(HttpStatus.OK)
	public void delete(@CurrentMember AuthorizationMember admin, @PathVariable("couponId") Long couponId) {
		couponService.delete(admin, couponId);
	}

	@GetMapping("/coupons/{couponId}")
	@ResponseStatus(HttpStatus.OK)
	public CouponResponse getById(@PathVariable("couponId") Long couponId) {
		return couponService.getById(couponId);
	}

	@PostMapping("/coupons/search")
	@ResponseStatus(HttpStatus.OK)
	public List<CouponResponse> getAllByStatus(@Valid @RequestBody CouponStatusRequest request) {
		return couponService.getAllByStatus(request);
	}

	@PostMapping("/coupons")
	public void registerQueue(@CurrentMember AuthorizationMember member,
		@RequestParam("couponName") String couponName) {
		couponQueueService.register(member, couponName);
	}
}
