package com.moabam.api.domain.room.repository;

import static com.moabam.api.domain.room.QParticipant.*;
import static com.moabam.api.domain.room.QRoom.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.moabam.api.domain.room.Participant;
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
					DynamicQuery.generateEq(memberId, participant.memberId::eq),
					participant.deletedAt.isNull()
				)
				.fetchOne()
		);
	}

	public List<Participant> findAllByRoomId(Long roomId) {
		return jpaQueryFactory
			.selectFrom(participant)
			.where(
				participant.room.id.eq(roomId),
				participant.deletedAt.isNull()
			)
			.fetch();
	}

	public List<Participant> findAllByMemberIdParticipant(Long memberId) {
		return jpaQueryFactory
			.selectFrom(participant)
			.where(
				participant.memberId.eq(memberId),
				participant.deletedAt.isNull()
			)
			.fetch();
	}

	public List<Participant> findAllWithDeletedByRoomId(Long roomId) {
		return jpaQueryFactory
			.selectFrom(participant)
			.where(
				participant.room.id.eq(roomId)
			)
			.fetch();
	}

	public List<Participant> findAllByRoomIdBeforeDate(Long roomId, LocalDateTime date) {
		return jpaQueryFactory
			.selectFrom(participant)
			.where(
				participant.room.id.eq(roomId),
				participant.createdAt.before(date),
				participant.deletedAt.isNull()
			)
			.fetch();
	}

	public List<Participant> findNotDeletedAllByMemberId(Long memberId) {
		return jpaQueryFactory
			.selectFrom(participant)
			.join(participant.room, room).fetchJoin()
			.where(
				participant.memberId.eq(memberId),
				participant.deletedAt.isNull()
			)
			.fetch();
	}

	public List<Participant> findAllByMemberId(Long memberId) {
		return jpaQueryFactory
			.selectFrom(participant)
			.leftJoin(participant.room, room).fetchJoin()
			.where(
				participant.memberId.eq(memberId)
			)
			.orderBy(participant.createdAt.desc())
			.fetch();
	}

	public List<Participant> findAllByRoomCertifyTime(int certifyTime) {
		return jpaQueryFactory
			.selectFrom(participant)
			.join(participant.room, room).fetchJoin()
			.where(
				participant.room.certifyTime.eq(certifyTime),
				participant.deletedAt.isNull()
			)
			.fetch();
	}

	public List<Participant> findAllRoomMangerByMemberId(Long memberId) {
		return jpaQueryFactory
			.selectFrom(participant)
			.join(participant.room, room).fetchJoin()
			.where(
				participant.memberId.eq(memberId),
				participant.isManager.isTrue()
			)
			.fetch();
	}
}
