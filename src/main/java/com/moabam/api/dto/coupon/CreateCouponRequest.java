package com.moabam.api.dto.coupon;

import java.time.LocalDate;

import org.hibernate.validator.constraints.Length;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record CreateCouponRequest(
	@NotBlank(message = "쿠폰명이 입력되지 않았거나 20자를 넘었습니다.") @Length(max = 20) String name,
	@Length(max = 50, message = "쿠폰 간단 소개는 최대 50자까지 가능합니다.") String description,
	@NotBlank(message = "쿠폰 종류를 입력해주세요.") String type,
	@Min(value = 1, message = "벌레 수 혹은 할인 금액은 1 이상이어야 합니다.") int point,
	@Min(value = 1, message = "쿠폰 재고는 1 이상이어야 합니다.") int stock,
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	@NotNull(message = "쿠폰 발급이 가능한 날짜(년, 월, 일)를 입력해주세요.") LocalDate startAt,
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	@NotNull(message = "쿠폰 정보창이 열리는 날짜(년, 월, 일)를 입력해주세요.") LocalDate openAt
) {

}
