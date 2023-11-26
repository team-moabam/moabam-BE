package com.moabam.api.dto.bug;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.*;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;

@Builder
public record BugHistoryItemResponse(
	Long id,
	String bugType,
	String actionType,
	int quantity,
	String date,
	@JsonInclude(NON_NULL) PaymentResponse payment

) {

	@Builder
	public record PaymentResponse(
		Long id,
		String orderName,
		int discountAmount,
		int totalAmount
	) {

	}
}
