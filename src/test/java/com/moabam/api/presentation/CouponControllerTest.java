package com.moabam.api.presentation;

import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moabam.api.application.coupon.CouponMapper;
import com.moabam.api.domain.coupon.Coupon;
import com.moabam.api.domain.coupon.CouponType;
import com.moabam.api.domain.coupon.CouponWallet;
import com.moabam.api.domain.coupon.repository.CouponRepository;
import com.moabam.api.domain.coupon.repository.CouponWalletRepository;
import com.moabam.api.domain.member.Role;
import com.moabam.api.domain.member.repository.MemberRepository;
import com.moabam.api.dto.coupon.CouponStatusRequest;
import com.moabam.api.dto.coupon.CreateCouponRequest;
import com.moabam.api.infrastructure.redis.ValueRedisRepository;
import com.moabam.api.infrastructure.redis.ZSetRedisRepository;
import com.moabam.global.common.util.ClockHolder;
import com.moabam.global.error.model.ErrorMessage;
import com.moabam.support.annotation.WithMember;
import com.moabam.support.common.WithoutFilterSupporter;
import com.moabam.support.fixture.CouponFixture;
import com.moabam.support.fixture.MemberFixture;
import com.moabam.support.snippet.CouponSnippet;
import com.moabam.support.snippet.CouponWalletSnippet;
import com.moabam.support.snippet.ErrorSnippet;

@Disabled
@Transactional
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
class CouponControllerTest extends WithoutFilterSupporter {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	MemberRepository memberRepository;

	@Autowired
	CouponRepository couponRepository;

	@Autowired
	CouponWalletRepository couponWalletRepository;

	@SpyBean
	ClockHolder clockHolder;

	@MockBean
	ZSetRedisRepository zSetRedisRepository;

	@MockBean
	ValueRedisRepository valueRedisRepository;

	@WithMember(role = Role.ADMIN)
	@DisplayName("POST - 쿠폰을 성공적으로 발행한다. - Void")
	@Test
	void create_Coupon_success() throws Exception {
		// Given
		CreateCouponRequest request = CouponFixture.createCouponRequest();

		given(clockHolder.date()).willReturn(LocalDate.of(2022, 1, 1));

		// When & Then
		mockMvc.perform(post("/admins/coupons")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andDo(print())
			.andDo(document("admins/coupons",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				CouponSnippet.CREATE_COUPON_REQUEST))
			.andExpect(status().isCreated());
	}

	@WithMember(role = Role.ADMIN)
	@DisplayName("POST - 현재 날짜가 쿠폰 발급 가능 날짜와 같거나 이후이다. - BadRequestException")
	@Test
	void create_Coupon_StartAt_BadRequestException() throws Exception {
		// Given
		CreateCouponRequest request = CouponFixture.createCouponRequest();

		given(clockHolder.date()).willReturn(LocalDate.of(2025, 1, 1));

		// When & Then
		mockMvc.perform(post("/admins/coupons")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andDo(print())
			.andDo(document("admins/coupons",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				CouponSnippet.CREATE_COUPON_REQUEST,
				ErrorSnippet.ERROR_MESSAGE_RESPONSE))
			.andExpect(status().isBadRequest())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.message").value(ErrorMessage.INVALID_COUPON_START_AT_PERIOD.getMessage()));
	}

	@WithMember(role = Role.ADMIN)
	@DisplayName("POST - 쿠폰 정보 오픈 날짜가 쿠폰 발급 시작 날짜와 같거나 이후인 쿠폰을 발행한다. - BadRequestException")
	@Test
	void create_Coupon_OpenAt_BadRequestException() throws Exception {
		// Given
		String couponType = CouponType.GOLDEN.getName();
		CreateCouponRequest request = CouponFixture.createCouponRequest(couponType, 1, 1);

		given(clockHolder.date()).willReturn(LocalDate.of(2022, 1, 1));

		// When & Then
		mockMvc.perform(post("/admins/coupons")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andDo(print())
			.andDo(document("admins/coupons",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				CouponSnippet.CREATE_COUPON_REQUEST,
				ErrorSnippet.ERROR_MESSAGE_RESPONSE))
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
				CouponSnippet.CREATE_COUPON_REQUEST,
				ErrorSnippet.ERROR_MESSAGE_RESPONSE))
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
				CouponSnippet.CREATE_COUPON_REQUEST,
				ErrorSnippet.ERROR_MESSAGE_RESPONSE))
			.andExpect(status().isConflict())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.message").value(ErrorMessage.CONFLICT_COUPON_START_AT.getMessage()));
	}

	@WithMember(role = Role.ADMIN)
	@DisplayName("DELETE - 쿠폰을 성공적으로 삭제한다. - Void")
	@Test
	void delete_Coupon_success() throws Exception {
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
				ErrorSnippet.ERROR_MESSAGE_RESPONSE))
			.andExpect(status().isNotFound())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.message").value(ErrorMessage.NOT_FOUND_COUPON.getMessage()));
	}

	@DisplayName("GET - 특정 쿠폰을 성공적으로 조회한다. - CouponResponse")
	@Test
	void getById_Coupon_success() throws Exception {
		// Given
		Coupon coupon = couponRepository.save(CouponFixture.coupon(10, 100));

		// When & Then
		mockMvc.perform(get("/coupons/" + coupon.getId()))
			.andDo(print())
			.andDo(document("coupons/couponId",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				CouponSnippet.COUPON_RESPONSE))
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
				ErrorSnippet.ERROR_MESSAGE_RESPONSE))
			.andExpect(status().isNotFound())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.message").value(ErrorMessage.NOT_FOUND_COUPON.getMessage()));
	}

	@DisplayName("POST - 모든 쿠폰을 조회한다. - List<CouponResponse>")
	@MethodSource("com.moabam.support.fixture.CouponFixture#provideCoupons")
	@ParameterizedTest
	void getAllByStatus_Coupons_success(List<Coupon> coupons) throws Exception {
		// Given
		CouponStatusRequest request = CouponFixture.couponStatusRequest(true, true);
		List<Coupon> coupon = couponRepository.saveAll(coupons);

		given(clockHolder.date()).willReturn(LocalDate.of(2022, 1, 1));

		// When & Then
		mockMvc.perform(post("/coupons/search")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andDo(print())
			.andDo(document("coupons/search",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				CouponSnippet.COUPON_STATUS_REQUEST,
				CouponSnippet.COUPON_STATUS_RESPONSE))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$", hasSize(coupon.size())));
	}

	@DisplayName("POST - 발급 가능한 쿠폰만 조회한다. - List<CouponResponse>")
	@MethodSource("com.moabam.support.fixture.CouponFixture#provideCoupons")
	@ParameterizedTest
	void getAllByStatus_Coupon_success(List<Coupon> coupons) throws Exception {
		// Given
		CouponStatusRequest request = CouponFixture.couponStatusRequest(false, false);
		couponRepository.saveAll(coupons);

		given(clockHolder.date()).willReturn(LocalDate.of(2023, 3, 1));

		// When & Then
		mockMvc.perform(post("/coupons/search")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andDo(print())
			.andDo(document("coupons/search",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				CouponSnippet.COUPON_STATUS_REQUEST))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$", hasSize(1)));
	}

	@WithMember
	@DisplayName("GET - 나의 쿠폰함에서 특정 쿠폰을 조회한다. - List<MyCouponResponse>")
	@Test
	void getAllByWalletIdAndMemberId_success() throws Exception {
		// Given
		Coupon coupon = couponRepository.save(CouponFixture.coupon());
		CouponWallet couponWallet = couponWalletRepository.save(CouponWallet.create(1L, coupon));

		// When & Then
		mockMvc.perform(get("/my-coupons/" + couponWallet.getId()))
			.andDo(print())
			.andDo(document("my-coupons/couponId",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				CouponWalletSnippet.COUPON_WALLET_RESPONSE))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$", hasSize(1)))
			.andExpect(jsonPath("$[0].id").value(coupon.getId()))
			.andExpect(jsonPath("$[0].name").value(coupon.getName()));
	}

	@WithMember
	@DisplayName("GET - 나의 쿠폰 보관함에 있는 모든 쿠폰을 조회한다. - List<MyCouponResponse>")
	@MethodSource("com.moabam.support.fixture.CouponWalletFixture#provideCouponWalletByCouponId1_total5")
	@ParameterizedTest
	void getAllByWalletIdAndMemberId_all_success(List<CouponWallet> couponWallets) throws Exception {
		// Given
		couponWallets.forEach(couponWallet -> {
			Coupon coupon = couponRepository.save(couponWallet.getCoupon());
			couponWalletRepository.save(CouponWallet.create(1L, coupon));
		});

		// When & Then
		mockMvc.perform(get("/my-coupons"))
			.andDo(print())
			.andDo(document("my-coupons/couponId",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				CouponWalletSnippet.COUPON_WALLET_RESPONSE))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$", hasSize(couponWallets.size())));
	}

	@WithMember
	@DisplayName("GET - 쿠폰이 없는 사용자의 쿠폰함을 조회한다. - List<MyCouponResponse>")
	@Test
	void getAllByWalletIdAndMemberId_no_coupon() throws Exception {
		// When & Then
		mockMvc.perform(get("/my-coupons"))
			.andDo(print())
			.andDo(document("my-coupons/couponId",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint())))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$", hasSize(0)));
	}

	@WithMember
	@DisplayName("POST - 특정 회원이 보유한 쿠폰을 성공적으로 사용한다. - Void")
	@Test
	void use_success() throws Exception {
		// Given
		Coupon coupon = couponRepository.save(CouponFixture.coupon());
		CouponWallet couponWallet = couponWalletRepository.save(CouponWallet.create(1L, coupon));
		memberRepository.save(MemberFixture.member(1L));

		// When & Then
		mockMvc.perform(post("/my-coupons/" + couponWallet.getId()))
			.andDo(print())
			.andDo(document("my-coupons/couponWalletId",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint())))
			.andExpect(status().isOk());
	}

	@WithMember
	@DisplayName("POST - 특정 회원이 보유하지 않은 쿠폰을 사용한다. - NotFoundException")
	@Test
	void use_NotFoundException() throws Exception {
		// When & Then
		mockMvc.perform(post("/my-coupons/" + 777L))
			.andDo(print())
			.andDo(document("my-coupons/couponWalletId",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				ErrorSnippet.ERROR_MESSAGE_RESPONSE))
			.andExpect(status().isNotFound())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.message").value(ErrorMessage.NOT_FOUND_COUPON_WALLET.getMessage()));
		;
	}

	@WithMember
	@DisplayName("POST - 특정 회원이 보유한 할인 쿠폰을 사용한다. - BadRequestException")
	@Test
	void use_BadRequestException() throws Exception {
		// Given
		Coupon coupon = couponRepository.save(CouponFixture.coupon(CouponType.DISCOUNT, 1000));
		CouponWallet couponWallet = couponWalletRepository.save(CouponWallet.create(1L, coupon));

		// When & Then
		mockMvc.perform(post("/my-coupons/" + couponWallet.getId()))
			.andDo(print())
			.andDo(document("my-coupons/couponWalletId",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				ErrorSnippet.ERROR_MESSAGE_RESPONSE))
			.andExpect(status().isBadRequest())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.message").value(ErrorMessage.INVALID_DISCOUNT_COUPON.getMessage()));
	}

	@WithMember
	@DisplayName("POST - 쿠폰 발급을 성공적으로 한다. - Void")
	@Test
	void registerQueue_success() throws Exception {
		// Given
		Coupon couponFixture = CouponFixture.coupon("CouponName", 2, 1);
		Coupon coupon = couponRepository.save(couponFixture);

		given(clockHolder.date()).willReturn(LocalDate.of(2023, 2, 1));
		given(zSetRedisRepository.score(anyString(), anyLong())).willReturn(null);
		given(zSetRedisRepository.size(anyString())).willReturn((long)(coupon.getMaxCount() - 1));

		// When & Then
		mockMvc.perform(post("/coupons")
				.param("couponName", coupon.getName()))
			.andDo(print())
			.andDo(document("coupons",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint())))
			.andExpect(status().isOk());
	}

	@WithMember
	@DisplayName("POST - 발급 가능 날짜가 아닌 쿠폰에 발급 요청을 한다. - NotFoundException")
	@Test
	void registerQueue_NotFoundException() throws Exception {
		// Given
		Coupon couponFixture = CouponFixture.coupon();
		Coupon coupon = couponRepository.save(couponFixture);

		given(clockHolder.date()).willReturn(LocalDate.of(2022, 1, 1));

		// When & Then
		mockMvc.perform(post("/coupons")
				.param("couponName", coupon.getName()))
			.andDo(print())
			.andDo(document("coupons",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				ErrorSnippet.ERROR_MESSAGE_RESPONSE))
			.andExpect(status().isNotFound())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.message").value(ErrorMessage.INVALID_COUPON_PERIOD.getMessage()));
	}

	@WithMember
	@DisplayName("POST - 동일한 쿠폰 이벤트에 중복으로 요청한다. - ConflictException")
	@Test
	void registerQueue_ConflictException() throws Exception {
		// Given
		Coupon coupon = couponRepository.save(CouponFixture.coupon());

		given(clockHolder.date()).willReturn(LocalDate.of(2023, 2, 1));
		given(zSetRedisRepository.score(anyString(), anyLong())).willReturn(7.0);

		// When & Then
		mockMvc.perform(post("/coupons")
				.param("couponName", coupon.getName()))
			.andDo(print())
			.andDo(document("coupons",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				ErrorSnippet.ERROR_MESSAGE_RESPONSE))
			.andExpect(status().isConflict())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.message").value(ErrorMessage.CONFLICT_COUPON_ISSUE.getMessage()));
	}

	@WithMember
	@DisplayName("POST - 선착순 이벤트가 마감된 쿠폰에 발급 요청을 한다. - BadRequestException")
	@Test
	void registerQueue_BadRequestException() throws Exception {
		// Given
		Coupon couponFixture = CouponFixture.coupon();
		Coupon coupon = couponRepository.save(couponFixture);

		given(clockHolder.date()).willReturn(LocalDate.of(2023, 2, 1));
		given(zSetRedisRepository.score(anyString(), anyLong())).willReturn(null);
		given(zSetRedisRepository.size(anyString())).willReturn((long)(coupon.getMaxCount()));

		// When & Then
		mockMvc.perform(post("/coupons")
				.param("couponName", coupon.getName()))
			.andDo(print())
			.andDo(document("coupons",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				ErrorSnippet.ERROR_MESSAGE_RESPONSE))
			.andExpect(status().isBadRequest())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.message").value(ErrorMessage.INVALID_COUPON_STOCK_END.getMessage()));
	}
}
