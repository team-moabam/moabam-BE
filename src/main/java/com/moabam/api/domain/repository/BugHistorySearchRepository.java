package com.moabam.api.domain.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.moabam.api.domain.bug.BugActionType;
import com.moabam.api.domain.bug.BugHistory;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class BugHistorySearchRepository {

	private final JPAQueryFactory jpaQueryFactory;

	public List<BugHistory> find(Long memberId, BugActionType actionType, LocalDateTime dateTime) {
		// return jpaQueryFactory
		// 	.selectFrom(bugHistory)
		// 	.where(
		// 		DynamicQuery.generateEq(memberId, bugHistory.memberId::eq),
		// 		DynamicQuery.generateEq(actionType, bugHistory.actionType::eq),
		// 		DynamicQuery.generateEq(dateTime, this::equalDate)
		// 	)
		// 	.fetch();
		return null;
	}

	private BooleanExpression equalDate(LocalDateTime dateTime) {
		return null;
		// return bugHistory.createdAt.year().eq(dateTime.getYear())
		// 	.and(bugHistory.createdAt.month().eq(dateTime.getMonthValue()))
		// 	.and(bugHistory.createdAt.dayOfMonth().eq(dateTime.getDayOfMonth()));
	}
}
