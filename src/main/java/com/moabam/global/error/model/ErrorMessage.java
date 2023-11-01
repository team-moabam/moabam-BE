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
	PARTICIPANT_NOT_FOUND("방에 대한 참여자의 정보가 없습니다."),

	LOGIN_FAILED("로그인에 실패했습니다."),
	REQUEST_FAILED("네트우크 접근 실패입니다."),
	GRANT_FAILED("인가 코드 실패"),
	MEMBER_NOT_FOUND("존재하지 않는 회원입니다."),

	INVALID_BUG_COUNT("벌레 개수는 0 이상이어야 합니다."),
	INVALID_PRICE("가격은 0 이상이어야 합니다."),
	INVALID_QUANTITY("수량은 1 이상이어야 합니다.");

	private final String message;
}
