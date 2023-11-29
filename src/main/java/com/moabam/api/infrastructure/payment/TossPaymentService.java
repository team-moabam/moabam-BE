package com.moabam.api.infrastructure.payment;

import java.util.Base64;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.moabam.api.dto.payment.ConfirmPaymentRequest;
import com.moabam.api.dto.payment.ConfirmTossPaymentResponse;
import com.moabam.global.config.TossPaymentConfig;
import com.moabam.global.error.exception.TossPaymentException;
import com.moabam.global.error.model.ErrorResponse;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TossPaymentService {

	private final TossPaymentConfig config;
	private WebClient webClient;

	@PostConstruct
	public void init() {
		this.webClient = WebClient.builder()
			.baseUrl(config.baseUrl())
			.defaultHeaders(headers -> {
				headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
				headers.setBearerAuth(Base64.getEncoder().encodeToString(config.secretKey().getBytes()));
			})
			.build();
	}

	@Transactional
	public ConfirmTossPaymentResponse confirm(ConfirmPaymentRequest request) {
		return webClient.post()
			.uri("/v1/payments/confirm")
			.body(BodyInserters.fromValue(request))
			.retrieve()
			.onStatus(HttpStatusCode::isError, response -> response.bodyToMono(ErrorResponse.class)
				.flatMap(error -> Mono.error(new TossPaymentException(error.message()))))
			.bodyToMono(ConfirmTossPaymentResponse.class)
			.block();
	}
}
