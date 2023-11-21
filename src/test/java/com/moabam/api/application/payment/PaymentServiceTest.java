package com.moabam.api.application.payment;

import static com.moabam.support.fixture.PaymentFixture.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.moabam.api.domain.payment.repository.PaymentRepository;
import com.moabam.api.domain.product.repository.ProductRepository;
import com.moabam.api.dto.payment.PaymentRequest;
import com.moabam.global.error.exception.NotFoundException;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

	@InjectMocks
	PaymentService paymentService;

	@Mock
	ProductRepository productRepository;

	@Mock
	PaymentRepository paymentRepository;

	@DisplayName("결제를 요청한다.")
	@Nested
	class RequestPayment {

		@DisplayName("해당 결제 정보가 존재하지 않으면 예외가 발생한다.")
		@Test
		void payment_not_found_exception() {
			// given
			Long memberId = 1L;
			Long paymentId = 1L;
			PaymentRequest request = new PaymentRequest(ORDER_ID);
			given(paymentRepository.findById(paymentId)).willReturn(Optional.empty());

			// when, then
			assertThatThrownBy(() -> paymentService.request(memberId, paymentId, request))
				.isInstanceOf(NotFoundException.class)
				.hasMessage("존재하지 않는 결제 정보입니다.");
		}
	}
}
