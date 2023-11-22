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
import com.moabam.api.dto.coupon.CouponStatusRequest;
import com.moabam.api.dto.coupon.CreateCouponRequest;
import com.moabam.global.common.util.ClockHolder;
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
	private ClockHolder clockHolder;

	@WithMember(role = Role.ADMIN)
	@DisplayName("POST - 쿠폰을 성공적으로 발행한다. - Void")
	@Test
	void create_Coupon() throws Exception {
		// Given
		CreateCouponRequest request = CouponFixture.createCouponRequest();
		given(clockHolder.times()).willReturn(LocalDateTime.of(2022, 1, 1, 1, 1));

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
	@DisplayName("POST - 현재 날짜가 쿠폰 발급 가능 날짜와 같거나 이후이다. - BadRequestException")
	@Test
	void create_Coupon_StartAt_BadRequestException() throws Exception {
		// Given
		CreateCouponRequest request = CouponFixture.createCouponRequest();

		given(clockHolder.times()).willReturn(LocalDateTime.of(2025, 1, 1, 1, 1));

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
			.andExpect(jsonPath("$.message").value(ErrorMessage.INVALID_COUPON_START_AT_PERIOD.getMessage()));
	}

	@WithMember(role = Role.ADMIN)
	@DisplayName("POST - 쿠폰 정보 오픈 날짜가 쿠폰 발급 시작 날짜와 같거나 이후인 쿠폰을 발행한다. - BadRequestException")
	@Test
	void create_Coupon_OpenAt_BadRequestException() throws Exception {
		// Given
		String couponType = CouponType.GOLDEN_COUPON.getName();
		CreateCouponRequest request = CouponFixture.createCouponRequest(couponType, 1, 1);

		given(clockHolder.times()).willReturn(LocalDateTime.of(2022, 1, 1, 1, 1));

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
			.andExpect(jsonPath("$.message").value(ErrorMessage.INVALID_COUPON_OPEN_AT_PERIOD.getMessage()));
	}

	@WithMember(role = Role.ADMIN)
	@DisplayName("POST - 쿠폰명이 중복된 쿠폰을 발행한다. - ConflictException")
	@Test
	void create_Coupon_Name_ConflictException() throws Exception {
		// Given
		CreateCouponRequest request = CouponFixture.createCouponRequest();
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
	@DisplayName("POST - 쿠폰 발행 가능 날짜가 중복된 쿠폰을 발행한다. - ConflictException")
	@Test
	void create_Coupon_StartAt_ConflictException() throws Exception {
		// Given
		CreateCouponRequest request = CouponFixture.createCouponRequest();
		Coupon conflictStartAtCoupon = CouponFixture.coupon("NotConflictName", 2, 1);
		couponRepository.save(conflictStartAtCoupon);

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
			.andExpect(jsonPath("$.message").value(ErrorMessage.CONFLICT_COUPON_START_AT.getMessage()));
	}

	@WithMember(role = Role.ADMIN)
	@DisplayName("DELETE - 쿠폰을 성공적으로 삭제한다. - Void")
	@Test
	void delete_Coupon() throws Exception {
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
	@DisplayName("DELETE - 존재하지 않는 쿠폰을 삭제한다. - NotFoundException")
	@Test
	void delete_Coupon_NotFoundException() throws Exception {
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

	@DisplayName("GET - 특정 쿠폰을 조회한다. - CouponResponse")
	@Test
	void getById_Coupon() throws Exception {
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
			.andExpect(jsonPath("$.id").value(coupon.getId()));
	}

	@DisplayName("GET - 존재하지 않는 쿠폰을 조회한다. - NotFoundException")
	@Test
	void getById_Coupon_NotFoundException() throws Exception {
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

	@DisplayName("POST - 모든 쿠폰을 조회한다. - List<CouponResponse>")
	@MethodSource("com.moabam.support.fixture.CouponFixture#provideCoupons")
	@ParameterizedTest
	void getAllByStatus_Coupons(List<Coupon> coupons) throws Exception {
		// Given
		CouponStatusRequest request = CouponFixture.couponStatusRequest(true, true);
		List<Coupon> coupon = couponRepository.saveAll(coupons);

		given(clockHolder.times()).willReturn(LocalDateTime.of(2022, 1, 1, 1, 1));

		// When & Then
		mockMvc.perform(post("/coupons/search")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andDo(print())
			.andDo(document("coupons/search",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				CouponSnippetFixture.COUPON_STATUS_REQUEST,
				CouponSnippetFixture.COUPON_STATUS_RESPONSE))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$", hasSize(coupon.size())));
	}

	@DisplayName("POST - 발급 가능한 쿠폰만 조회한다.. - List<CouponResponse>")
	@MethodSource("com.moabam.support.fixture.CouponFixture#provideCoupons")
	@ParameterizedTest
	void getAllByStatus_Coupon(List<Coupon> coupons) throws Exception {
		// Given
		CouponStatusRequest request = CouponFixture.couponStatusRequest(false, false);
		couponRepository.saveAll(coupons);

		given(clockHolder.times()).willReturn(LocalDateTime.of(2023, 3, 1, 1, 1));

		// When & Then
		mockMvc.perform(post("/coupons/search")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andDo(print())
			.andDo(document("coupons/search",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				CouponSnippetFixture.COUPON_STATUS_REQUEST))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$", hasSize(1)));
	}

	@WithMember(nickname = "member-coupon-1")
	@DisplayName("POST - 쿠폰 발급 요청을 한다. - Void")
	@Test
	void registerQueue() throws Exception {
		// Given
		Coupon couponFixture = CouponFixture.coupon();
		Coupon coupon = couponRepository.save(couponFixture);

		given(clockHolder.times()).willReturn(LocalDateTime.of(2023, 2, 1, 1, 1));

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
	@DisplayName("POST - 발급 기간이 아닌 쿠폰에 발급 요청을 한다. - BadRequestException")
	@Test
	void registerQueue_BadRequestException() throws Exception {
		// Given
		Coupon couponFixture = CouponFixture.coupon();
		Coupon coupon = couponRepository.save(couponFixture);

		given(clockHolder.times()).willReturn(LocalDateTime.of(2022, 2, 1, 1, 1));

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
			.andExpect(jsonPath("$.message").value(ErrorMessage.INVALID_COUPON_PERIOD.getMessage()));
	}

	@WithMember
	@DisplayName("POST - 존재하지 않는 쿠폰에 발급 요청을 한다. - NotFoundException")
	@Test
	void registerQueue_NotFoundException() throws Exception {
		// Given
		Coupon coupon = CouponFixture.coupon("Not found coupon name", 2, 1);

		given(clockHolder.times()).willReturn(LocalDateTime.of(2023, 2, 1, 1, 1));

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
