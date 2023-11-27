package com.moabam.api.presentation;

import static com.moabam.global.auth.model.AuthorizationThreadLocal.*;
import static com.moabam.support.fixture.MemberFixture.*;
import static com.moabam.support.fixture.PaymentFixture.*;
import static com.moabam.support.fixture.ProductFixture.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.http.MediaType.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moabam.api.application.member.MemberService;
import com.moabam.api.domain.payment.Payment;
import com.moabam.api.domain.payment.PaymentStatus;
import com.moabam.api.domain.payment.repository.PaymentRepository;
import com.moabam.api.domain.product.Product;
import com.moabam.api.domain.product.repository.ProductRepository;
import com.moabam.api.dto.payment.ConfirmPaymentRequest;
import com.moabam.api.dto.payment.PaymentRequest;
import com.moabam.api.infrastructure.payment.TossPaymentService;
import com.moabam.support.annotation.WithMember;
import com.moabam.support.common.WithoutFilterSupporter;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class PaymentControllerTest extends WithoutFilterSupporter {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	ObjectMapper objectMapper;

	@MockBean
	MemberService memberService;

	@MockBean
	TossPaymentService tossPaymentService;

	@Autowired
	PaymentRepository paymentRepository;

	@Autowired
	ProductRepository productRepository;

	@Nested
	@DisplayName("결제를 요청한다.")
	class Request {

		@DisplayName("성공한다.")
		@WithMember
		@Test
		void success() throws Exception {
			// given
			Product product = productRepository.save(bugProduct());
			Payment payment = paymentRepository.save(payment(product));
			PaymentRequest request = new PaymentRequest(ORDER_ID);

			// expected
			mockMvc.perform(post("/payments/{paymentId}", payment.getId())
					.contentType(APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andDo(print());
			Payment actual = paymentRepository.findById(payment.getId()).orElseThrow();
			assertThat(actual.getOrder().getId()).isEqualTo(ORDER_ID);
		}

		@DisplayName("결제 요청 바디가 유효하지 않으면 예외가 발생한다.")
		@WithMember
		@ParameterizedTest
		@NullAndEmptySource
		void bad_request_body_exception(String orderId) throws Exception {
			// given
			Long paymentId = 1L;
			PaymentRequest request = new PaymentRequest(orderId);

			// expected
			mockMvc.perform(post("/payments/{paymentId}", paymentId)
					.contentType(APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("올바른 요청 정보가 아닙니다."))
				.andDo(print());
		}
	}

	@Nested
	@DisplayName("결제를 승인한다.")
	class Confirm {

		@DisplayName("성공한다.")
		@WithMember
		@Test
		void success() throws Exception {
			// given
			Long memberId = getAuthMember().id();
			Product product = productRepository.save(bugProduct());
			Payment payment = paymentRepository.save(payment(product));
			payment.request(ORDER_ID);
			ConfirmPaymentRequest request = confirmPaymentRequest();
			given(tossPaymentService.confirm(confirmTossPaymentRequest())).willReturn(confirmTossPaymentResponse());
			given(memberService.findMember(memberId)).willReturn(member());

			// expected
			mockMvc.perform(post("/payments/confirm")
					.contentType(APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andDo(print());
			Payment actual = paymentRepository.findById(payment.getId()).orElseThrow();
			assertThat(actual.getStatus()).isEqualTo(PaymentStatus.DONE);
		}

		@DisplayName("결제 승인 요청 바디가 유효하지 않으면 예외가 발생한다.")
		@WithMember
		@ParameterizedTest
		@CsvSource(value = {
			", random_order_id_123, 2000",
			"payment_key_123, , 2000",
			"payment_key_123, random_order_id_123, -1000",
		})
		void bad_request_body_exception(String paymentKey, String orderId, int amount) throws Exception {
			// given
			ConfirmPaymentRequest request = new ConfirmPaymentRequest(paymentKey, orderId, amount);

			// expected
			mockMvc.perform(post("/payments/confirm")
					.contentType(APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("올바른 요청 정보가 아닙니다."))
				.andDo(print());
		}
	}
}
