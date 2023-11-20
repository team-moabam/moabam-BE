package com.moabam.api.domain.member.repository;

import static com.moabam.api.domain.member.QMember.*;
import static com.moabam.api.domain.room.QParticipant.*;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.moabam.api.domain.member.Member;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class MemberSearchRepository {

	private final JPAQueryFactory jpaQueryFactory;

	public Optional<Member> findManager(Long roomId) {
		return Optional.ofNullable(
			jpaQueryFactory
				.selectFrom(member)
				.innerJoin(participant).on(member.id.eq(participant.memberId))
				.where(
					isNotDeleted(),
					participant.isManager.eq(true),
					participant.room.id.eq(roomId)
				)
				.fetchOne()
		);
	}

	public Optional<Member> findMember(Long memberId) {
		return Optional.ofNullable(jpaQueryFactory
			.selectFrom(member)
			.where(
				isNotDeleted(),
				member.id.eq(memberId)
			)
			.fetchOne());
	}

	private BooleanExpression isNotDeleted() {
		return member.deletedAt.isNotNull();
	}
}
