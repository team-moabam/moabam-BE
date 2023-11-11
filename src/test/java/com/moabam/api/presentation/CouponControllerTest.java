package com.moabam.api.presentation;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moabam.api.domain.entity.Coupon;
import com.moabam.api.domain.entity.enums.CouponType;
import com.moabam.api.domain.repository.CouponRepository;
import com.moabam.api.dto.CouponMapper;
import com.moabam.api.dto.CreateCouponRequest;
import com.moabam.support.common.WithoutFilterSupporter;
import com.moabam.support.fixture.CouponFixture;
import com.moabam.support.fixture.CouponSnippetFixture;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
class CouponControllerTest extends WithoutFilterSupporter {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private CouponRepository couponRepository;

	@DisplayName("쿠폰을 성공적으로 발행한다. - Void")
	@Test
	void couponController_createCoupon() throws Exception {
		// Given
		String couponType = CouponType.GOLDEN_COUPON.getTypeName();
		CreateCouponRequest request = CouponFixture.createCouponRequest(couponType, 1, 2);

		// When & Then
		mockMvc.perform(post("/admins/coupons")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andDo(print())
			.andDo(document("admins/coupons",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				CouponSnippetFixture.CREATE_COUPON_REQUEST))
			.andExpect(status().isCreated());
	}

	@DisplayName("쿠폰명이 중복된 쿠폰을 발행한다. - ConflictException")
	@Test
	void couponController_createCoupon_ConflictException() throws Exception {
		// Given
		String couponType = CouponType.GOLDEN_COUPON.getTypeName();
		CreateCouponRequest request = CouponFixture.createCouponRequest(couponType, 1, 2);
		couponRepository.save(CouponMapper.toEntity(1L, request));

		// When & Then
		mockMvc.perform(post("/admins/coupons")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andDo(print())
			.andDo(document("admins/coupons",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				CouponSnippetFixture.CREATE_COUPON_REQUEST))
			.andExpect(status().isConflict());
	}

	@DisplayName("쿠폰을 성공적으로 삭제한다. - Void")
	@Test
	void couponController_deleteCoupon() throws Exception {
		// Given
		Coupon coupon = couponRepository.save(CouponFixture.coupon(10, 100));

		// When & Then
		mockMvc.perform(delete("/admins/coupons/" + coupon.getId()))
			.andDo(print())
			.andDo(document("admins/coupons/couponId",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint())))
			.andExpect(status().isOk());
	}

	@DisplayName("존재하지 않는 쿠폰을 삭제한다. - NotFoundException")
	@Test
	void couponController_deleteCoupon_NotFoundException() throws Exception {
		// When & Then
		mockMvc.perform(delete("/admins/coupons/77777777777"))
			.andDo(print())
			.andDo(document("admins/coupons/couponId",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint())))
			.andExpect(status().isNotFound());
	}
}
