package com.moabam.api.domain.member.repository;

import static com.moabam.api.domain.member.QBadge.*;
import static com.moabam.api.domain.member.QMember.*;
import static com.moabam.api.domain.room.QRoom.*;
import static com.querydsl.core.group.GroupBy.*;
import static java.lang.Boolean.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.moabam.api.domain.member.Member;
import com.moabam.api.dto.member.MemberInfoSearchResponse;
import com.moabam.global.common.util.DynamicQuery;
import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Projections;
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

	public List<MemberInfoSearchResponse> findMemberAndBadges(Long searchId, boolean isMe) {
		List<Expression<?>> selectExpression = new ArrayList<>(List.of(
			member.nickname,
			member.profileImage,
			member.intro,
			member.totalCertifyCount,
			set(badge.type)));

		if (isMe) {
			selectExpression.addAll(List.of(
				member.bug.goldenBug,
				member.bug.morningBug,
				member.bug.nightBug));
		}

		return jpaQueryFactory
			.from(member)
			.leftJoin(badge).on(member.id.eq(badge.memberId))
			.where(
				DynamicQuery.generateIsNull(true, member.deletedAt),
				member.id.eq(searchId)
			)
			.transform(GroupBy.groupBy(member.id).list(Projections.constructor(MemberInfoSearchResponse.class,
				selectExpression.toArray(new Expression<?>[0])
			)));
	}
}
