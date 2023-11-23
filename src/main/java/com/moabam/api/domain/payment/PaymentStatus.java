package com.moabam.api.domain.payment;

/**
 * READY: 결제 생성
 * IN_PROGRESS: 결제 인증 완료
 * DONE: 결제 승인 완료
 * CANCELED: 승인된 결제 취소
 * ABORTED: 결제 승인 실패
 * EXPIRED: 유효 시간 경과로 거래 취소
 */
public enum PaymentStatus {

	READY,
	IN_PROGRESS,
	DONE,
	CANCELED,
	ABORTED,
	EXPIRED;
}
