package com.moabam.api.presentation;

import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moabam.api.application.coupon.CouponMapper;
import com.moabam.api.domain.coupon.Coupon;
import com.moabam.api.domain.coupon.CouponType;
import com.moabam.api.domain.coupon.repository.CouponRepository;
import com.moabam.api.domain.member.Role;
import com.moabam.api.dto.coupon.CouponSearchRequest;
import com.moabam.api.dto.coupon.CreateCouponRequest;
import com.moabam.global.common.util.SystemClockHolder;
import com.moabam.global.error.model.ErrorMessage;
import com.moabam.support.annotation.WithMember;
import com.moabam.support.common.WithoutFilterSupporter;
import com.moabam.support.fixture.CouponFixture;
import com.moabam.support.fixture.CouponSnippetFixture;
import com.moabam.support.fixture.ErrorSnippetFixture;

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

	@MockBean
	private SystemClockHolder systemClockHolder;

	@WithMember(role = Role.ADMIN)
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

	@WithMember(role = Role.ADMIN)
	@DisplayName("쿠폰 발급 종료기간 시작기간보다 이전인 쿠폰을 발행한다. - BadRequestException")
	@Test
	void couponController_createCoupon_BadRequestException() throws Exception {
		// Given
		String couponType = CouponType.GOLDEN_COUPON.getTypeName();
		CreateCouponRequest request = CouponFixture.createCouponRequest(couponType, 2, 1);

		// When & Then
		mockMvc.perform(post("/admins/coupons")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andDo(print())
			.andDo(document("admins/coupons",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				CouponSnippetFixture.CREATE_COUPON_REQUEST,
				ErrorSnippetFixture.ERROR_MESSAGE_RESPONSE))
			.andExpect(status().isBadRequest())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.message").value(ErrorMessage.INVALID_COUPON_PERIOD.getMessage()));
	}

	@WithMember(role = Role.ADMIN)
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
				CouponSnippetFixture.CREATE_COUPON_REQUEST,
				ErrorSnippetFixture.ERROR_MESSAGE_RESPONSE))
			.andExpect(status().isConflict())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.message").value(ErrorMessage.CONFLICT_COUPON_NAME.getMessage()));
	}

	@WithMember(role = Role.ADMIN)
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

	@WithMember(role = Role.ADMIN)
	@DisplayName("존재하지 않는 쿠폰을 삭제한다. - NotFoundException")
	@Test
	void couponController_deleteCoupon_NotFoundException() throws Exception {
		// When & Then
		mockMvc.perform(delete("/admins/coupons/77777777777"))
			.andDo(print())
			.andDo(document("admins/coupons/couponId",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				ErrorSnippetFixture.ERROR_MESSAGE_RESPONSE))
			.andExpect(status().isNotFound())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.message").value(ErrorMessage.NOT_FOUND_COUPON.getMessage()));
	}

	@DisplayName("특정 쿠폰을 조회한다. - CouponResponse")
	@Test
	void couponController_getCouponById() throws Exception {
		// Given
		Coupon coupon = couponRepository.save(CouponFixture.coupon(10, 100));

		// When & Then
		mockMvc.perform(get("/coupons/" + coupon.getId()))
			.andDo(print())
			.andDo(document("coupons/couponId",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				CouponSnippetFixture.COUPON_RESPONSE))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.couponId").value(coupon.getId()));
	}

	@DisplayName("존재하지 않는 쿠폰을 조회한다. - NotFoundException")
	@Test
	void couponController_getCouponById_NotFoundException() throws Exception {
		// When & Then
		mockMvc.perform(get("/coupons/77777777777"))
			.andDo(print())
			.andDo(document("coupons/couponId",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				ErrorSnippetFixture.ERROR_MESSAGE_RESPONSE))
			.andExpect(status().isNotFound())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.message").value(ErrorMessage.NOT_FOUND_COUPON.getMessage()));
	}

	@DisplayName("모든 쿠폰을 조회한다. - List<CouponResponse>")
	@MethodSource("com.moabam.support.fixture.CouponFixture#provideCoupons")
	@ParameterizedTest
	void couponController_getCoupons(List<Coupon> coupons) throws Exception {
		// Given
		CouponSearchRequest request = CouponFixture.couponSearchRequest(true, true, true);
		List<Coupon> coupon = couponRepository.saveAll(coupons);

		// When & Then
		mockMvc.perform(post("/coupons/search")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andDo(print())
			.andDo(document("coupons/search",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				CouponSnippetFixture.COUPON_SEARCH_REQUEST,
				CouponSnippetFixture.COUPON_SEARCH_RESPONSE))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$", hasSize(coupon.size())));
	}

	@DisplayName("상태 조건을 걸지 않아서 쿠폰이 조회되지 않는다. - List<CouponResponse>")
	@MethodSource("com.moabam.support.fixture.CouponFixture#provideCoupons")
	@ParameterizedTest
	void couponController_getCoupons_not_status(List<Coupon> coupons) throws Exception {
		// Given
		CouponSearchRequest request = CouponFixture.couponSearchRequest(false, false, false);
		couponRepository.saveAll(coupons);

		// When & Then
		mockMvc.perform(post("/coupons/search")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andDo(print())
			.andDo(document("coupons/search",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				CouponSnippetFixture.COUPON_SEARCH_REQUEST))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$", hasSize(0)));
	}

	@WithMember(nickname = "member-coupon-1")
	@DisplayName("쿠폰 발급 요청을 한다. - Void")
	@Test
	void couponController_registerCouponQueue() throws Exception {
		// Given
		Coupon couponFixture = CouponFixture.coupon("CouponName", 1, 2);
		LocalDateTime now = LocalDateTime.of(2023, 1, 1, 1, 1);
		Coupon coupon = couponRepository.save(couponFixture);

		given(systemClockHolder.times()).willReturn(now);

		// When & Then
		mockMvc.perform(post("/coupons")
				.param("couponName", coupon.getName()))
			.andDo(print())
			.andDo(document("coupons",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint())))
			.andExpect(status().isOk());
	}

	@WithMember(nickname = "member-coupon-2")
	@DisplayName("발급 기간이 아닌 쿠폰에 발급 요청을 한다. - BadRequestException")
	@Test
	void couponController_registerCouponQueue_BadRequestException() throws Exception {
		// Given
		Coupon couponFixture = CouponFixture.coupon("CouponName", 1, 2);
		LocalDateTime now = LocalDateTime.of(2022, 1, 1, 1, 1);
		Coupon coupon = couponRepository.save(couponFixture);

		given(systemClockHolder.times()).willReturn(now);

		// When & Then
		mockMvc.perform(post("/coupons")
				.param("couponName", coupon.getName()))
			.andDo(print())
			.andDo(document("coupons",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				ErrorSnippetFixture.ERROR_MESSAGE_RESPONSE))
			.andExpect(status().isBadRequest())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.message").value(ErrorMessage.INVALID_COUPON_PERIOD_END.getMessage()));
	}

	@WithMember
	@DisplayName("존재하지 않는 쿠폰에 발급 요청을 한다. - NotFoundException")
	@Test
	void couponController_registerCouponQueue_NotFoundException() throws Exception {
		// Given
		Coupon coupon = CouponFixture.coupon("Not found coupon name", 1, 2);
		LocalDateTime now = LocalDateTime.of(2023, 1, 1, 1, 1);

		given(systemClockHolder.times()).willReturn(now);

		// When & Then
		mockMvc.perform(post("/coupons")
				.param("couponName", coupon.getName()))
			.andDo(print())
			.andDo(document("coupons",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				ErrorSnippetFixture.ERROR_MESSAGE_RESPONSE))
			.andExpect(status().isNotFound())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.message").value(ErrorMessage.NOT_FOUND_COUPON.getMessage()));
	}
}
