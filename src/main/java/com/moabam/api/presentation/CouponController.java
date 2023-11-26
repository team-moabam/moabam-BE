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

import com.moabam.api.application.coupon.CouponManageService;
import com.moabam.api.application.coupon.CouponService;
import com.moabam.api.dto.coupon.CouponResponse;
import com.moabam.api.dto.coupon.CouponStatusRequest;
import com.moabam.api.dto.coupon.CreateCouponRequest;
import com.moabam.api.dto.coupon.MyCouponResponse;
import com.moabam.global.auth.annotation.Auth;
import com.moabam.global.auth.model.AuthMember;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class CouponController {

	private final CouponService couponService;
	private final CouponManageService couponManageService;

	@PostMapping("/admins/coupons")
	@ResponseStatus(HttpStatus.CREATED)
	public void create(@Auth AuthMember admin, @Valid @RequestBody CreateCouponRequest request) {
		couponService.create(admin, request);
	}

	@DeleteMapping("/admins/coupons/{couponId}")
	@ResponseStatus(HttpStatus.OK)
	public void delete(@Auth AuthMember admin, @PathVariable("couponId") Long couponId) {
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
	@ResponseStatus(HttpStatus.OK)
	public void registerQueue(@Auth AuthMember authMember, @RequestParam("couponName") String couponName) {
		couponManageService.register(authMember, couponName);
	}

	@GetMapping({"/my-coupons", "/my-coupons/{couponId}"})
	@ResponseStatus(HttpStatus.OK)
	public List<MyCouponResponse> getWallet(@Auth AuthMember authMember,
		@PathVariable(value = "couponId", required = false) Long couponId) {
		return couponService.getWallet(couponId, authMember);
	}

	@PostMapping("/my-coupons/{couponId}")
	@ResponseStatus(HttpStatus.OK)
	public void use(@Auth AuthMember authMember, @PathVariable("couponId") Long couponId) {
		couponService.use(authMember, couponId);
	}
}
