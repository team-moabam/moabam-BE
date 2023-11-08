package com.moabam.api.domain.repository;

import static com.moabam.api.domain.entity.QRoutine.*;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.moabam.api.domain.entity.Routine;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class RoutineSearchRepository {

	private final JPAQueryFactory jpaQueryFactory;

	public List<Routine> findByRoomId(Long roomId) {
		return jpaQueryFactory
			.selectFrom(routine)
			.where(
				routine.room.id.eq(roomId)
			)
			.fetch();
	}
}
