package com.moabam.api.domain.room.repository;

import static com.moabam.api.domain.room.QRoom.*;
import static com.moabam.api.domain.room.QRoutine.*;
import static com.moabam.global.common.util.GlobalConstant.*;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.moabam.api.domain.room.Room;
import com.moabam.api.domain.room.RoomType;
import com.moabam.global.common.util.DynamicQuery;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class RoomSearchRepository {

	private static final double MATCH_THRESHOLD = 0.0;
	private static final String MATCH_AGAINST_TEMPLATE = "function('match_against', {0}, {1})";

	private final JPAQueryFactory jpaQueryFactory;

	public List<Room> findAllWithNoOffset(RoomType roomType, Long roomId) {
		return jpaQueryFactory.selectFrom(room)
			.where(
				DynamicQuery.generateEq(roomType, room.roomType::eq),
				DynamicQuery.generateEq(roomId, room.id::lt),
				room.deletedAt.isNull()
			)
			.orderBy(room.id.desc())
			.limit(ROOM_FIXED_SEARCH_SIZE + 1L)
			.fetch();
	}

	public List<Room> searchWithKeyword(String keyword, RoomType roomType, Long roomId) {
		return jpaQueryFactory.selectFrom(room)
			.distinct()
			.leftJoin(routine).on(room.id.eq(routine.room.id))
			.where(
				matchAgainst(keyword),
				DynamicQuery.generateEq(roomType, room.roomType::eq),
				DynamicQuery.generateEq(roomId, room.id::lt),
				room.deletedAt.isNull()
			)
			.orderBy(room.id.desc())
			.limit(11)
			.fetch();
	}

	private BooleanExpression matchAgainst(String keyword) {
		keyword = "\"" + keyword + "\"";

		return Expressions.numberTemplate(Double.class, MATCH_AGAINST_TEMPLATE, room.title, keyword)
			.gt(MATCH_THRESHOLD)
			.or(Expressions.numberTemplate(Double.class, MATCH_AGAINST_TEMPLATE, room.managerNickname, keyword)
				.gt(MATCH_THRESHOLD))
			.or(Expressions.numberTemplate(Double.class, MATCH_AGAINST_TEMPLATE, routine.content, keyword)
				.gt(MATCH_THRESHOLD));
	}
}
