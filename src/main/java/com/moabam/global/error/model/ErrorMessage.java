package com.moabam.global.error.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorMessage {

	INVALID_REQUEST_FIELD("올바른 요청 정보가 아닙니다."),
	ROOM_NOT_FOUND("존재하지 않는 방 입니다."),
	ROOM_MAX_USER_COUNT_MODIFY_FAIL("잘못된 최대 인원수 설정입니다."),
	ROOM_MODIFY_UNAUTHORIZED_REQUEST("방장이 아닌 사용자는 방을 수정할 수 없습니다."),
	PARTICIPANT_NOT_FOUND("방에 대한 참여자의 정보가 없습니다.");

	private final String message;
}
