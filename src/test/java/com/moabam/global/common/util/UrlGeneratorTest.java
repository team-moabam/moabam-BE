package com.moabam.global.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class UrlGeneratorTest {

	private static final String BASE_URL = "http://localhost:8080/auth";
	private static final String RESPONSE_TYPE = "response-type";
	private static final String CODE = "code";

	@DisplayName("베이스 URL이 없으면 생성 실패")
	@Test
	void create_url_failBy_no_base_url() {
		// When + Then
		assertThatThrownBy(UrlGenerator.builder()::build)
			.isInstanceOf(NullPointerException.class);
	}

	@DisplayName("URL 생성 성공")
	@Test
	void create_url_success() {
		assertThatNoException().isThrownBy(() -> {
			// Given
			UrlGenerator urlGenerator = UrlGenerator.builder()
				.baseUrl(BASE_URL).build();

			// When
			String url = urlGenerator.generateUrl();

			// Then
			assertThat(url).isEqualTo(BASE_URL);
		});
	}

	@DisplayName("쿼리가 포함된 url 생성 성공")
	@Test
	void create_query_url_success() {
		assertThatNoException().isThrownBy(() -> {
			// Given
			UrlGenerator urlGenerator = UrlGenerator.builder()
				.baseUrl(BASE_URL)
				.parameter(RESPONSE_TYPE, CODE).build();

			// When
			String url = urlGenerator.generateUrl();

			// Then
			assertThat(url).isEqualTo(BASE_URL + "?" + RESPONSE_TYPE + "=" + CODE);
		});
	}
}
