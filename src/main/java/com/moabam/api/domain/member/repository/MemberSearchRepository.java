package com.moabam.api.domain.member.repository;

import static com.moabam.api.domain.member.QMember.*;
import static com.moabam.api.domain.room.QRoom.*;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.moabam.api.domain.member.Member;
import com.moabam.global.common.util.DynamicQuery;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class MemberSearchRepository {

	private final JPAQueryFactory jpaQueryFactory;

	public Optional<Member> findMember(Long memberId) {
		return findMember(memberId, true);
	}

	public Optional<Member> findMember(Long memberId, boolean isNotDeleted) {
		return Optional.ofNullable(jpaQueryFactory
			.selectFrom(member)
			.where(
				DynamicQuery.generateIsNull(isNotDeleted, member.deletedAt),
				member.id.eq(memberId)
			)
			.fetchOne());
	}

	public Optional<Member> findMemberNotManager(Long memberId) {
		return Optional.ofNullable(jpaQueryFactory
			.selectFrom(member)
			.where(
				member.id.eq(memberId),
				JPAExpressions.selectOne()
					.from(room)
					.where(
						member.nickname.eq(room.managerNickname)
					)
					.notExists()
			)
			.fetchFirst());
	}
}
