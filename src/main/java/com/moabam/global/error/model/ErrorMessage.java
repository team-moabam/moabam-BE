package com.moabam.global.error.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorMessage {

	INVALID_REQUEST_FIELD("올바른 요청 정보가 아닙니다."),

	MEMBER_NOT_FOUND("존재하지 않는 회원입니다."),

	INVALID_BUG_COUNT("벌레 개수는 0 이상이어야 합니다.");

	private final String message;
}
