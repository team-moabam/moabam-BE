package com.moabam.api.domain.bug.repository;

import static com.moabam.api.domain.bug.QBugHistory.*;
import static com.moabam.api.domain.payment.QPayment.*;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.moabam.api.domain.item.repository.BugHistoryDto;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class BugHistorySearchRepository {

	private final JPAQueryFactory jpaQueryFactory;

	public List<BugHistoryDto> findByMemberIdWithPayment(Long memberId) {
		return jpaQueryFactory.select(Projections.constructor(
				BugHistoryDto.class,
				bugHistory.id,
				bugHistory.bugType,
				bugHistory.actionType,
				bugHistory.quantity,
				bugHistory.createdAt,
				payment)
			)
			.from(bugHistory)
			.leftJoin(bugHistory.payment, payment)
			.where(bugHistory.memberId.eq(memberId))
			.orderBy(bugHistory.createdAt.desc())
			.fetch();
	}
}
