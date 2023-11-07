package com.moabam.api.domain.repository;

import static com.moabam.api.domain.entity.QInventory.*;
import static com.moabam.api.domain.entity.QItem.*;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.moabam.api.domain.entity.Inventory;
import com.moabam.api.domain.entity.Item;
import com.moabam.api.domain.entity.enums.ItemType;
import com.moabam.global.common.util.DynamicQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class InventorySearchRepository {

	private final JPAQueryFactory jpaQueryFactory;

	public Optional<Inventory> findOne(Long memberId, Long itemId) {
		return Optional.ofNullable(
			jpaQueryFactory.selectFrom(inventory)
				.where(
					DynamicQuery.generateEq(memberId, inventory.memberId::eq),
					DynamicQuery.generateEq(itemId, inventory.item.id::eq))
				.fetchOne()
		);
	}

	public Optional<Inventory> findDefault(Long memberId, ItemType type) {
		return Optional.ofNullable(
			jpaQueryFactory.selectFrom(inventory)
				.where(
					DynamicQuery.generateEq(memberId, inventory.memberId::eq),
					DynamicQuery.generateEq(type, inventory.item.type::eq),
					inventory.isDefault.isTrue())
				.fetchOne()
		);
	}

	public List<Item> findItems(Long memberId, ItemType type) {
		return jpaQueryFactory.selectFrom(inventory)
			.join(inventory.item, item)
			.where(
				DynamicQuery.generateEq(memberId, inventory.memberId::eq),
				DynamicQuery.generateEq(type, inventory.item.type::eq))
			.orderBy(inventory.createdAt.desc())
			.select(item)
			.fetch();
	}
}
