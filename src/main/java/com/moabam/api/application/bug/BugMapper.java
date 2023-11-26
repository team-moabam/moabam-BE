package com.moabam.api.application.bug;

import java.util.List;
import java.util.Optional;

import com.moabam.api.domain.bug.Bug;
import com.moabam.api.domain.bug.BugActionType;
import com.moabam.api.domain.bug.BugHistory;
import com.moabam.api.domain.bug.BugType;
import com.moabam.api.domain.item.repository.BugHistoryDto;
import com.moabam.api.dto.bug.BugHistoryItemResponse;
import com.moabam.api.dto.bug.BugHistoryResponse;
import com.moabam.api.dto.bug.BugResponse;
import com.moabam.global.common.util.DateUtils;
import com.moabam.global.common.util.StreamUtils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BugMapper {

	public static BugHistory toUseBugHistory(Long memberId, BugType bugType, int quantity) {
		return BugHistory.builder()
			.memberId(memberId)
			.bugType(bugType)
			.actionType(BugActionType.USE)
			.quantity(quantity)
			.build();
	}

	public static BugResponse toBugResponse(Bug bug) {
		return BugResponse.builder()
			.morningBug(bug.getMorningBug())
			.nightBug(bug.getNightBug())
			.goldenBug(bug.getGoldenBug())
			.build();
	}

	public static BugHistoryItemResponse toBugHistoryItemResponse(BugHistoryDto dto) {
		BugHistoryItemResponse.PaymentResponse payment = Optional.ofNullable(dto.payment())
			.map(p -> BugHistoryItemResponse.PaymentResponse.builder()
				.id(p.getId())
				.orderName(p.getOrder().getName())
				.discountAmount(p.getDiscountAmount())
				.totalAmount(p.getTotalAmount())
				.build())
			.orElse(null);

		return BugHistoryItemResponse.builder()
			.id(dto.id())
			.bugType(dto.bugType())
			.actionType(dto.actionType())
			.quantity(dto.quantity())
			.date(DateUtils.format(dto.createdAt()))
			.payment(payment)
			.build();
	}

	public static BugHistoryResponse toBugHistoryResponse(List<BugHistoryDto> dtoList) {
		return BugHistoryResponse.builder()
			.history(StreamUtils.map(dtoList, BugMapper::toBugHistoryItemResponse))
			.build();
	}
}
