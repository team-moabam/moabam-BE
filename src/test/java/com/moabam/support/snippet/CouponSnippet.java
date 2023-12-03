package com.moabam.support.snippet;

import static org.springframework.restdocs.payload.JsonFieldType.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;

import org.springframework.restdocs.payload.RequestFieldsSnippet;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;
import org.springframework.restdocs.snippet.Snippet;

public final class CouponSnippet {

	public static final RequestFieldsSnippet CREATE_COUPON_REQUEST = requestFields(
		fieldWithPath("name").type(STRING).description("쿠폰명"),
		fieldWithPath("description").type(STRING).description("쿠폰 간단 소개 (NULL 가능)"),
		fieldWithPath("type").type(STRING).description("쿠폰 종류 (아침, 저녁, 황금, 할인)"),
		fieldWithPath("point").type(NUMBER).description("쿠폰 사용 시, 제공하는 포인트량"),
		fieldWithPath("maxCount").type(NUMBER).description("쿠폰을 발급 최대 갯수"),
		fieldWithPath("startAt").type(STRING).description("쿠폰 발급 시작 날짜 (Ex: yyyy-MM-dd)"),
		fieldWithPath("openAt").type(STRING).description("쿠폰 정보 오픈 날짜 (Ex: yyyy-MM-dd)")
	);

	public static final ResponseFieldsSnippet COUPON_RESPONSE = responseFields(
		fieldWithPath("id").type(NUMBER).description("쿠폰 ID"),
		fieldWithPath("adminId").type(NUMBER).description("쿠폰 관리자 ID"),
		fieldWithPath("name").type(STRING).description("쿠폰명"),
		fieldWithPath("description").type(STRING).description("쿠폰에 대한 간단 소개 (NULL 가능)"),
		fieldWithPath("point").type(NUMBER).description("쿠폰 사용 시, 제공하는 포인트량"),
		fieldWithPath("maxCount").type(NUMBER).description("쿠폰을 발급 최대 갯수"),
		fieldWithPath("type").type(STRING).description("쿠폰 종류 (MORNING, NIGHT, GOLDEN, DISCOUNT)"),
		fieldWithPath("startAt").type(STRING).description("쿠폰 발급 시작 날짜 (Ex: yyyy-MM-dd)"),
		fieldWithPath("openAt").type(STRING).description("쿠폰 정보 오픈 날짜 (Ex: yyyy-MM-dd)")
	);

	public static final Snippet COUPON_STATUS_REQUEST = requestFields(
		fieldWithPath("opened").type(BOOLEAN).description("쿠폰 정보가 오픈된 쿠폰 (true, false)"),
		fieldWithPath("ended").type(BOOLEAN).description("종료된 쿠폰 (true, false)")
	);

	public static final ResponseFieldsSnippet COUPON_STATUS_RESPONSE = responseFields(
		fieldWithPath("[].id").type(NUMBER).description("쿠폰 ID"),
		fieldWithPath("[].adminId").type(NUMBER).description("쿠폰 관리자 ID"),
		fieldWithPath("[].name").type(STRING).description("쿠폰명"),
		fieldWithPath("[].description").type(STRING).description("쿠폰에 대한 간단 소개 (NULL 가능)"),
		fieldWithPath("[].point").type(NUMBER).description("쿠폰 사용 시, 제공하는 포인트량"),
		fieldWithPath("[].maxCount").type(NUMBER).description("쿠폰을 발급 최대 갯수"),
		fieldWithPath("[].type").type(STRING).description("쿠폰 종류 (MORNING, NIGHT, GOLDEN, DISCOUNT)"),
		fieldWithPath("[].startAt").type(STRING).description("쿠폰 발급 시작 날짜 (Ex: yyyy-MM-dd)"),
		fieldWithPath("[].openAt").type(STRING).description("쿠폰 정보 오픈 날짜 (Ex: yyyy-MM-dd)")
	);
}
