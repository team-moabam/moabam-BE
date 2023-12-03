package com.moabam.api.domain.room;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 방 경험치
 * 방 레벨 - 현재 경험치 / 전체 경험치
 * 레벨0 - 0 / 1
 * 레벨1 - 0 / 3
 * 레벨2 - 0 / 5
 * 레벨3 - 0 / 10
 */

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum RoomExp {

	ROOM_LEVEL_0(0, 1),
	ROOM_LEVEL_1(1, 5),
	ROOM_LEVEL_2(2, 10),
	ROOM_LEVEL_3(3, 20),
	ROOM_LEVEL_4(4, 40),
	ROOM_LEVEL_5(5, 80);

	private static final Map<Integer, String> requireExpMap = Collections.unmodifiableMap(
		Stream.of(values())
			.collect(Collectors.toMap(RoomExp::getLevel, RoomExp::name))
	);

	private final int level;
	private final int totalExp;

	public static RoomExp of(int level) {
		return RoomExp.valueOf(requireExpMap.get(level));
	}
}
