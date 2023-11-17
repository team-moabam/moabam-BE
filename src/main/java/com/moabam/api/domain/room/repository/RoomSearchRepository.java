package com.moabam.api.domain.room.repository;

import static com.moabam.api.domain.room.QRoom.*;
import static com.moabam.global.common.util.GlobalConstant.*;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.moabam.api.domain.room.Room;
import com.moabam.api.domain.room.RoomType;
import com.moabam.global.common.util.DynamicQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class RoomSearchRepository {

	private final JPAQueryFactory jpaQueryFactory;

	public List<Room> findAllWithNoOffset(RoomType roomType, Long roomId) {
		return jpaQueryFactory.selectFrom(room)
			.where(
				DynamicQuery.generateEq(roomType, room.roomType::eq),
				DynamicQuery.generateEq(roomId, room.id::lt)
			)
			.orderBy(room.id.desc())
			.limit(ROOM_FIXED_SEARCH_SIZE + 1)
			.fetch();
	}
}
