package com.moabam.support.snippet;

import static org.springframework.restdocs.payload.JsonFieldType.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;

import org.springframework.restdocs.payload.ResponseFieldsSnippet;

public final class CouponWalletSnippet {

	public static final ResponseFieldsSnippet COUPON_WALLET_RESPONSE = responseFields(
		fieldWithPath("[].id").type(NUMBER).description("쿠폰 ID"),
		fieldWithPath("[].name").type(STRING).description("쿠폰명"),
		fieldWithPath("[].description").type(STRING).description("쿠폰에 대한 간단 소개 (NULL 가능)"),
		fieldWithPath("[].point").type(NUMBER).description("쿠폰 사용 시, 제공하는 포인트량"),
		fieldWithPath("[].type").type(STRING)
			.description("쿠폰 종류 (MORNING_COUPON, NIGHT_COUPON, GOLDEN_COUPON, DISCOUNT_COUPON)")
	);
}
