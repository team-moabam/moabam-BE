package com.moabam.api.dto.bug;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.*;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.moabam.api.domain.bug.BugActionType;
import com.moabam.api.domain.bug.BugType;
import com.moabam.api.dto.payment.PaymentResponse;

import lombok.Builder;

@Builder
public record BugHistoryItemResponse(
	Long id,
	BugType bugType,
	BugActionType actionType,
	int quantity,
	String date,
	@JsonInclude(NON_NULL) PaymentResponse payment
) {

}
