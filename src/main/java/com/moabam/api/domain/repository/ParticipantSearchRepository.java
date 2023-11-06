package com.moabam.api.domain.repository;

import static com.moabam.api.domain.entity.QParticipant.*;
import static com.moabam.api.domain.entity.QRoom.*;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.moabam.api.domain.entity.Participant;
import com.moabam.global.common.util.DynamicQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ParticipantSearchRepository {

	private final JPAQueryFactory jpaQueryFactory;

	public Optional<Participant> findOne(Long memberId, Long roomId) {
		return Optional.ofNullable(
			jpaQueryFactory
				.selectFrom(participant)
				.join(participant.room, room).fetchJoin()
				.where(
					DynamicQuery.generateEq(roomId, participant.room.id::eq),
					DynamicQuery.generateEq(memberId, participant.memberId::eq)
				)
				.fetchOne()
		);
	}

	public List<Participant> findParticipants(Long roomId) {
		return jpaQueryFactory
			.selectFrom(participant)
			.where(
				participant.room.id.eq(roomId)
			)
			.fetch();
	}
}
