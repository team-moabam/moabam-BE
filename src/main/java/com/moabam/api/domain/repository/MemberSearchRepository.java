package com.moabam.api.domain.repository;

import static com.moabam.api.domain.entity.QMember.*;
import static com.moabam.api.domain.entity.QParticipant.*;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.moabam.api.domain.entity.Member;
import com.moabam.global.common.util.DynamicQuery;
import com.querydsl.jpa.JPAExpressions;
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
				.where(
					member.id.eq(
						JPAExpressions
							.select(participant.memberId)
							.from(participant)
							.where(
								DynamicQuery.generateEq(true, participant.isManager::eq),
								DynamicQuery.generateEq(member.id, participant.memberId::eq),
								DynamicQuery.generateEq(roomId, participant.room.id::eq)
							)
					)
				)
				.fetchOne()
		);
	}
}
