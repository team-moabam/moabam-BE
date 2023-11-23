package com.moabam.api.domain.coupon;

import com.moabam.global.common.entity.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "coupon_wallet")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CouponWallet extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "member_id", updatable = false, nullable = false)
	private Long memberId;

	@JoinColumn(name = "coupon_id", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	private Coupon coupon;

	@Builder
	private CouponWallet(Long memberId, Coupon coupon) {
		this.memberId = memberId;
		this.coupon = coupon;
	}
}
