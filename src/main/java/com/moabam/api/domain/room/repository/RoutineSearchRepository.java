package com.moabam.api.domain.room.repository;

import static com.moabam.api.domain.room.QRoutine.*;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.moabam.api.domain.room.Routine;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class RoutineSearchRepository {

	private final JPAQueryFactory jpaQueryFactory;

	public List<Routine> findAllByRoomId(Long roomId) {
		return jpaQueryFactory
			.selectFrom(routine)
			.where(
				routine.room.id.eq(roomId)
			)
			.fetch();
	}

	public List<Routine> findAllByRoomIds(List<Long> roomIds) {
		return jpaQueryFactory
			.selectFrom(routine)
			.where(
				routine.room.id.in(roomIds)
			)
			.fetch();
	}
}
