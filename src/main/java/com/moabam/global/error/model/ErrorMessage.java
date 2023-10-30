package com.moabam.global.error.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorMessage {

	INVALID_REQUEST_FIELD("올바른 요청 정보가 아닙니다."),
	LOGIN_FAILED("로그인에 실패했습니다.");

	private final String message;
}
