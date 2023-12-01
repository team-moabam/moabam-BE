package com.moabam.api.domain.member.repository;

import static com.moabam.api.domain.member.QBadge.*;
import static com.moabam.api.domain.member.QMember.*;
import static com.moabam.api.domain.room.QParticipant.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.moabam.api.domain.member.Member;
import com.moabam.api.dto.member.MemberInfo;
import com.moabam.global.common.util.DynamicQuery;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class MemberSearchRepository {

	private final JPAQueryFactory jpaQueryFactory;

	public Optional<Member> findMember(Long memberId) {
		return findMember(memberId, true);
	}

	public List<Member> findAllMembers() {
		return jpaQueryFactory
			.selectFrom(member)
			.where(
				member.deletedAt.isNotNull()
			)
			.fetch();
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
			.leftJoin(participant).on(member.id.eq(participant.memberId))
			.where(
				member.id.eq(memberId),
				participant.isManager.isNull().or(participant.isManager.isFalse())
			)
			.fetchFirst());
	}

	public List<MemberInfo> findMemberAndBadges(Long searchId, boolean isMe) {
		List<Expression<?>> selectExpression = new ArrayList<>(List.of(
			member.nickname,
			member.profileImage,
			member.morningImage,
			member.nightImage,
			member.intro,
			member.totalCertifyCount,
			badge.type));

		if (isMe) {
			selectExpression.addAll(List.of(
				member.bug.goldenBug,
				member.bug.morningBug,
				member.bug.nightBug));
		}

		return jpaQueryFactory
			.select(Projections.constructor(MemberInfo.class, selectExpression.toArray(new Expression<?>[0])))
			.from(member)
			.leftJoin(badge).on(member.id.eq(badge.memberId))
			.where(
				DynamicQuery.generateIsNull(true, member.deletedAt),
				member.id.eq(searchId)
			).fetch();
	}
}
