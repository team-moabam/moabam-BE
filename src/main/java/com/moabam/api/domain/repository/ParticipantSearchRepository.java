package com.moabam.api.domain.repository;

import static com.moabam.api.domain.entity.QParticipant.*;

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

	public Optional<Participant> findParticipant(Long memberId, Long roomId) {
		return Optional.ofNullable(
			jpaQueryFactory.selectFrom(participant)
				.where(
					DynamicQuery.generateEq(roomId, participant.room.id::eq),
					DynamicQuery.generateEq(memberId, participant.memberId::eq)
				).fetchOne()
		);
	}
}
