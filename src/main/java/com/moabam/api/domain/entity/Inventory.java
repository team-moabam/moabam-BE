package com.moabam.api.domain.entity;

import static java.util.Objects.*;

import org.hibernate.annotations.ColumnDefault;

import com.moabam.api.domain.entity.enums.ItemType;
import com.moabam.global.common.entity.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "inventory", indexes = @Index(name = "idx_member_id", columnList = "member_id"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Inventory extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "member_id", updatable = false, nullable = false)
	private Long memberId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "item_id", updatable = false, nullable = false)
	private Item item;

	@Column(name = "is_default", nullable = false)
	@ColumnDefault("false")
	private boolean isDefault;

	@Builder
	private Inventory(Long memberId, Item item, boolean isDefault) {
		this.memberId = requireNonNull(memberId);
		this.item = requireNonNull(item);
		this.isDefault = isDefault;
	}

	public ItemType getItemType() {
		return this.item.getType();
	}

	public void select() {
		this.isDefault = true;
	}

	public void deselect() {
		this.isDefault = false;
	}
}
