package com.moabam.api.domain.item.repository;

import static com.moabam.api.domain.item.QInventory.inventory;
import static com.moabam.api.domain.item.QItem.item;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.moabam.api.domain.item.Item;
import com.moabam.api.domain.item.ItemType;
import com.moabam.global.common.util.DynamicQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ItemSearchRepository {

	private final JPAQueryFactory jpaQueryFactory;

	public List<Item> findNotPurchasedItems(Long memberId, ItemType type) {
		return jpaQueryFactory.selectFrom(item)
			.leftJoin(inventory)
			.on(inventory.item.id.eq(item.id)
				.and(inventory.memberId.eq(memberId)))
			.where(
				DynamicQuery.generateEq(type, item.type::eq),
				inventory.memberId.isNull()
			)
			.orderBy(
				item.unlockLevel.asc(),
				item.bugPrice.asc(),
				item.goldenBugPrice.asc(),
				item.name.asc())
			.fetch();
	}
}
