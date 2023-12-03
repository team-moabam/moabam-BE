package com.moabam.api.dto.coupon;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

class CreateCouponRequestTest {

	@DisplayName("쿠폰 발급 가능 시작 날짜가 올바른 형식으로 입력된다. - yyyy-MM-dd")
	@Test
	void startAt_success() throws JsonProcessingException {
		// Given
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());

		String json = "{\"startAt\":\"2023-11-09\"}";

		// When
		CreateCouponRequest actual = objectMapper.readValue(json, CreateCouponRequest.class);

		// Then
		assertThat(actual.startAt()).isEqualTo(LocalDate.of(2023, 11, 9));
	}
}
