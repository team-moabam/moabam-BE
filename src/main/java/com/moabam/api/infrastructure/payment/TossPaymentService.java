package com.moabam.api.infrastructure.payment;

import static org.springframework.http.MediaType.*;

import java.util.Base64;

import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.moabam.api.dto.payment.ConfirmTossPaymentRequest;
import com.moabam.api.dto.payment.ConfirmTossPaymentResponse;
import com.moabam.global.config.TossPaymentConfig;
import com.moabam.global.error.exception.MoabamException;
import com.moabam.global.error.model.ErrorResponse;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class TossPaymentService {

	private final TossPaymentConfig config;
	private WebClient webClient;

	@PostConstruct
	public void init() {
		this.webClient = WebClient.builder()
			.baseUrl(config.baseUrl())
			.defaultHeaders(
				headers -> headers.setBearerAuth(Base64.getEncoder().encodeToString(config.secretKey().getBytes())))
			.build();
	}

	public ConfirmTossPaymentResponse confirm(ConfirmTossPaymentRequest request) {
		return webClient.post()
			.uri("/v1/payments/confirm")
			.contentType(APPLICATION_JSON)
			.body(BodyInserters.fromValue(request))
			.retrieve()
			.onStatus(HttpStatusCode::isError, response -> response.bodyToMono(ErrorResponse.class)
				.flatMap(error -> Mono.error(new MoabamException(error.message()))))
			.bodyToMono(ConfirmTossPaymentResponse.class)
			.block();
	}
}
