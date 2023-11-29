package com.moabam.api.infrastructure.payment;

import static com.moabam.support.fixture.PaymentFixture.*;
import static org.assertj.core.api.Assertions.*;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moabam.api.dto.payment.ConfirmPaymentRequest;
import com.moabam.api.dto.payment.ConfirmTossPaymentResponse;
import com.moabam.global.config.TossPaymentConfig;
import com.moabam.global.error.exception.MoabamException;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

@SpringBootTest
@ActiveProfiles("test")
class TossPaymentServiceTest {

	@Autowired
	TossPaymentConfig config;

	@Autowired
	ObjectMapper objectMapper;

	TossPaymentService tossPaymentService;
	MockWebServer mockWebServer;

	@BeforeEach
	public void setup() {
		mockWebServer = new MockWebServer();
		tossPaymentService = new TossPaymentService(
			new TossPaymentConfig(mockWebServer.url("/").toString(), config.secretKey())
		);
		tossPaymentService.init();
	}

	@AfterEach
	public void tearDown() throws IOException {
		mockWebServer.shutdown();
	}

	@DisplayName("결제 승인을 요청한다.")
	@Nested
	class Confirm {

		@DisplayName("성공한다.")
		@Test
		void success() throws Exception {
			// given
			ConfirmPaymentRequest request = confirmPaymentRequest();
			ConfirmTossPaymentResponse expected = confirmTossPaymentResponse();
			mockWebServer.enqueue(new MockResponse()
				.setResponseCode(200)
				.setBody(objectMapper.writeValueAsString(expected))
				.addHeader("Content-Type", "application/json"));

			// when
			ConfirmTossPaymentResponse actual = tossPaymentService.confirm(request);

			// then
			assertThat(actual).isEqualTo(expected);
		}

		@DisplayName("예외가 발생한다.")
		@Test
		void exception() {
			// given
			ConfirmPaymentRequest request = confirmPaymentRequest();
			String jsonString = "{\"code\":\"NOT_FOUND_PAYMENT\",\"message\":\"존재하지 않는 결제 입니다.\"}";
			mockWebServer.enqueue(new MockResponse()
				.setResponseCode(404)
				.setBody(jsonString)
				.addHeader("Content-Type", "application/json"));

			// when, then
			assertThatThrownBy(() -> tossPaymentService.confirm(request))
				.isInstanceOf(MoabamException.class)
				.hasMessage("존재하지 않는 결제 입니다.");
		}
	}
}
