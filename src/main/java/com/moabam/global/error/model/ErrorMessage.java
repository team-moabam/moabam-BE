package com.moabam.global.error.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorMessage {

	FAILED_MOABAM("모아밤 서버 실행 중 오류가 발생했습니다."),
	INVALID_REQUEST_FIELD("올바른 요청 정보가 아닙니다."),
	INVALID_REQUEST_VALUE_TYPE_FORMAT("'%s' 값은 유효한 %s 값이 아닙니다."),
	NOT_FOUND_AVAILABLE_PORT("사용 가능한 포트를 찾을 수 없습니다. (10000 ~ 65535)"),
	ERROR_EXECUTING_EMBEDDED_REDIS("Embedded Redis 실행 중 오류가 발생했습니다."),

	ROOM_NOT_FOUND("존재하지 않는 방 입니다."),
	ROOM_MAX_USER_COUNT_MODIFY_FAIL("잘못된 최대 인원수 설정입니다."),
	ROOM_MODIFY_UNAUTHORIZED_REQUEST("방장이 아닌 사용자는 방을 수정할 수 없습니다."),
	ROOM_EXIT_MANAGER_FAIL("인원수가 2명 이상일 때는 방장을 위임해야 합니다."),
	PARTICIPANT_NOT_FOUND("방에 대한 참여자의 정보가 없습니다."),
	WRONG_ROOM_PASSWORD("방의 비밀번호가 일치하지 않습니다."),
	ROOM_MAX_USER_REACHED("방의 인원수가 찼습니다."),
	ROOM_DETAILS_ERROR("방 정보를 불러오는데 실패했습니다."),
	ROUTINE_LENGTH_ERROR("루틴의 길이가 잘못 되었습니다."),
	DUPLICATED_DAILY_MEMBER_CERTIFICATION("이미 오늘의 인증을 완료하였습니다."),
	ROUTINE_NOT_FOUND("루틴을 찾을 수 없습니다"),
	INVALID_REQUEST_URL("잘못된 URL 요청입니다."),
	INVALID_CERTIFY_TIME("현재 인증 시간이 아닙니다."),

	LOGIN_FAILED("로그인에 실패했습니다."),
	REQUEST_FAILED("네트워크 접근 실패입니다."),
	GRANT_FAILED("인가 코드 실패"),
	AUTHENTICATIE_FAIL("인증 실패"),
	MEMBER_NOT_FOUND("존재하지 않는 회원입니다."),
	MEMBER_ROOM_EXCEED("참여할 수 있는 방의 개수가 모두 찼습니다."),

	BUG_NOT_ENOUGH("보유한 벌레가 부족합니다."),

	ITEM_NOT_FOUND("존재하지 않는 아이템입니다."),
	ITEM_UNLOCK_LEVEL_HIGH("아이템 해금 레벨이 높습니다."),
	ITEM_NOT_PURCHASABLE_BY_BUG_TYPE("해당 벌레 타입으로는 구매할 수 없는 아이템입니다."),
	INVENTORY_NOT_FOUND("구매하지 않은 아이템은 적용할 수 없습니다."),
	INVENTORY_CONFLICT("이미 구매한 아이템입니다."),

	INVALID_BUG_COUNT("벌레 개수는 0 이상이어야 합니다."),
	INVALID_PRICE("가격은 0 이상이어야 합니다."),
	INVALID_QUANTITY("수량은 1 이상이어야 합니다."),
	INVALID_LEVEL("레벨은 1 이상이어야 합니다."),

	FAILED_FCM_INIT("파이어베이스 설정을 실패했습니다."),
	NOT_FOUND_FCM_TOKEN("해당 유저는 접속 중이 아닙니다."),
	CONFLICT_KNOCK("이미 콕 알림을 보낸 대상입니다."),

	INVALID_COUPON_POINT("쿠폰의 보너스 포인트는 0 이상이어야 합니다."),
	INVALID_COUPON_STOCK("쿠폰의 재고는 0 이상이어야 합니다."),
	CONFLICT_COUPON_NAME("쿠폰의 이름이 중복되었습니다."),
	NOT_FOUND_COUPON_TYPE("존재하지 않는 쿠폰 종류입니다."),
	INVALID_COUPON_PERIOD("쿠폰 발급 종료 시각은 시작 시각보다 이후여야 합니다."),

	S3_UPLOAD_FAIL("S3 업로드를 실패했습니다."),
	S3_INVALID_IMAGE("올바른 이미지(파일) 형식이 아닙니다."),
	S3_INVALID_IMAGE_SIZE("파일의 용량이 너무 큽니다."),
	S3_RESIZE_ERROR("이미지 리사이징에서 에러가 발생했습니다.");

	private final String message;
}
