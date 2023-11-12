package com.moabam.api.domain.entity;

import static com.moabam.global.common.util.GlobalConstant.*;
import static com.moabam.global.error.model.ErrorMessage.*;
import static java.util.Objects.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.hibernate.annotations.ColumnDefault;

import com.moabam.api.domain.entity.enums.CouponType;
import com.moabam.global.common.entity.BaseTimeEntity;
import com.moabam.global.error.exception.BadRequestException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "coupon")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Coupon extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "name", nullable = false, unique = true, length = 20)
	private String name;

	@ColumnDefault("1")
	@Column(name = "point", nullable = false)
	private int point;

	@ColumnDefault("''")
	@Column(name = "description", length = 50)
	private String description;

	@Enumerated(value = EnumType.STRING)
	@Column(name = "coupon_type", nullable = false)
	private CouponType couponType;

	@ColumnDefault("1")
	@Column(name = "stock", nullable = false)
	private int stock;

	@Column(name = "start_at", nullable = false)
	private LocalDateTime startAt;

	@Column(name = "end_at", nullable = false)
	private LocalDateTime endAt;

	// TODO : 관리자 테이블 생기면 관리자 테이블이랑 다대일 관계 맺을 예정
	@Column(name = "admin_id", updatable = false, nullable = false)
	private Long adminId;

	@Builder
	private Coupon(String name, int point, String description, CouponType couponType, int stock, LocalDateTime startAt,
		LocalDateTime endAt, Long adminId) {
		this.name = requireNonNull(name);
		this.point = validatePoint(point);
		this.description = Optional.ofNullable(description).orElse(BLANK);
		this.couponType = requireNonNull(couponType);
		this.stock = validateStock(stock);
		this.startAt = requireNonNull(startAt);
		this.endAt = requireNonNull(endAt);
		this.adminId = requireNonNull(adminId);
	}

	private int validatePoint(int point) {
		if (point < 1) {
			throw new BadRequestException(INVALID_COUPON_POINT);
		}

		return point;
	}

	private int validateStock(int stock) {
		if (stock < 1) {
			throw new BadRequestException(INVALID_COUPON_STOCK);
		}

		return stock;
	}

	@Override
	public String toString() {
		return "Coupon{startAt=" + startAt + ", endAt=" + endAt + '}';
	}
}
