package com.moabam.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "payment.toss")
public record TossPaymentConfig(
	String baseUrl,
	String secretKey
) {

}
