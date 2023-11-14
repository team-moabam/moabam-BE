package com.moabam.api.dto;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.moabam.api.dto.coupon.CreateCouponRequest;

class CreateCouponRequestTest {

	@DisplayName("쿠폰 발급 가능 시작 날짜가 올바른 형식으로 입력된다. - yyyy-MM-dd'T'HH:mm")
	@Test
	void createCouponRequest_StartAt() throws JsonProcessingException {
		// Given
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());

		String json = "{\"startAt\":\"2023-11-09T10:10\"}";

		// When
		CreateCouponRequest actual = objectMapper.readValue(json, CreateCouponRequest.class);

		// Then
		assertThat(actual.startAt()).isEqualTo(LocalDateTime.of(2023, 11, 9, 10, 10));
	}
}
