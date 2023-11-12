package com.moabam.support.fixture;

import static org.springframework.restdocs.payload.JsonFieldType.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;

import org.springframework.restdocs.payload.RequestFieldsSnippet;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;
import org.springframework.restdocs.snippet.Snippet;

public final class CouponSnippetFixture {

	public static final RequestFieldsSnippet CREATE_COUPON_REQUEST = requestFields(
		fieldWithPath("name").type(STRING).description("쿠폰명"),
		fieldWithPath("description").type(STRING).description("쿠폰 간단 소개 (NULL 가능)"),
		fieldWithPath("couponType").type(STRING).description("쿠폰 종류 (아침, 저녁, 황금, 할인)"),
		fieldWithPath("point").type(NUMBER).description("쿠폰 사용 시, 제공하는 포인트량"),
		fieldWithPath("stock").type(NUMBER).description("쿠폰을 발급 받을 수 있는 수"),
		fieldWithPath("startAt").type(STRING).description("쿠폰 발급 시작 날짜 (Ex: yyyy-MM-dd'T'HH:mm)"),
		fieldWithPath("endAt").type(STRING).description("쿠폰 발급 종료 날짜 (Ex: yyyy-MM-dd'T'HH:mm)")
	);

	public static final ResponseFieldsSnippet COUPON_RESPONSE = responseFields(
		fieldWithPath("couponId").type(NUMBER).description("쿠폰 ID"),
		fieldWithPath("couponAdminName").type(STRING).description("쿠폰 관리자명"),
		fieldWithPath("name").type(STRING).description("쿠폰명"),
		fieldWithPath("description").type(STRING).description("쿠폰에 대한 간단 소개 (NULL 가능)"),
		fieldWithPath("point").type(NUMBER).description("쿠폰 사용 시, 제공하는 포인트량"),
		fieldWithPath("stock").type(NUMBER).description("쿠폰을 발급 받을 수 있는 수"),
		fieldWithPath("couponType").type(STRING)
			.description("쿠폰 종류 (MORNING_COUPON, NIGHT_COUPON, GOLDEN_COUPON, DISCOUNT_COUPON)"),
		fieldWithPath("startAt").type(STRING).description("쿠폰 발급 시작 날짜 (Ex: yyyy-MM-dd'T'HH:mm)"),
		fieldWithPath("endAt").type(STRING).description("쿠폰 발급 종료 날짜 (Ex: yyyy-MM-dd'T'HH:mm)")
	);

	public static final Snippet COUPON_SEARCH_REQUEST = requestFields(
		fieldWithPath("couponOngoing").type(BOOLEAN).description("진행 상태 쿠폰 (true, false)"),
		fieldWithPath("couponNotStarted").type(BOOLEAN).description("시작전 상태 쿠폰 (true, false)"),
		fieldWithPath("couponEnded").type(BOOLEAN).description("종료 상태 쿠폰 (true, false)")
	);

	public static final ResponseFieldsSnippet COUPON_SEARCH_RESPONSE = responseFields(
		fieldWithPath("[].couponId").type(NUMBER).description("쿠폰 ID"),
		fieldWithPath("[].couponAdminName").type(STRING).description("쿠폰 관리자명"),
		fieldWithPath("[].name").type(STRING).description("쿠폰명"),
		fieldWithPath("[].description").type(STRING).description("쿠폰에 대한 간단 소개 (NULL 가능)"),
		fieldWithPath("[].point").type(NUMBER).description("쿠폰 사용 시, 제공하는 포인트량"),
		fieldWithPath("[].stock").type(NUMBER).description("쿠폰을 발급 받을 수 있는 수"),
		fieldWithPath("[].couponType").type(STRING)
			.description("쿠폰 종류 (MORNING_COUPON, NIGHT_COUPON, GOLDEN_COUPON, DISCOUNT_COUPON)"),
		fieldWithPath("[].startAt").type(STRING).description("쿠폰 발급 시작 날짜 (Ex: yyyy-MM-dd'T'HH:mm)"),
		fieldWithPath("[].endAt").type(STRING).description("쿠폰 발급 종료 날짜 (Ex: yyyy-MM-dd'T'HH:mm)")
	);
}
