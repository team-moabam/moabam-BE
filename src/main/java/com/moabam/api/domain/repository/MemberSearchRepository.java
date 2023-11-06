package com.moabam.api.domain.repository;

import static com.moabam.api.domain.entity.QMember.*;
import static com.moabam.api.domain.entity.QParticipant.*;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.moabam.api.domain.entity.Member;
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
					participant.isManager.eq(true),
					participant.room.id.eq(roomId)
				)
				.fetchOne()
		);
	}
}
