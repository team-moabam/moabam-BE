package com.moabam.api.dto.bug;

import java.time.LocalDateTime;

import com.moabam.api.domain.bug.BugActionType;
import com.moabam.api.domain.bug.BugType;
import com.moabam.api.domain.payment.Payment;

import lombok.Builder;

@Builder
public record BugHistoryWithPayment(
	Long id,
	BugType bugType,
	BugActionType actionType,
	int quantity,
	LocalDateTime createdAt,
	Payment payment
) {

}
