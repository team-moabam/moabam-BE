package com.moabam.api.domain.repository;

import static com.moabam.api.domain.entity.QInventory.*;
import static com.moabam.api.domain.entity.QItem.*;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.moabam.api.domain.entity.Item;
import com.moabam.api.domain.entity.enums.ItemType;
import com.moabam.global.common.util.DynamicQuery;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ItemSearchRepository {

	private final JPAQueryFactory jpaQueryFactory;

	public List<Item> findNotPurchasedItems(Long memberId, ItemType type) {
		return jpaQueryFactory.selectFrom(item)
			.leftJoin(inventory)
			.on(inventory.item.id.eq(item.id))
			.where(
				DynamicQuery.generateEq(type, item.type::eq),
				DynamicQuery.generateEq(memberId, this::filterByMemberId))
			.orderBy(
				item.unlockLevel.asc(),
				item.bugPrice.asc(),
				item.goldenBugPrice.asc(),
				item.name.asc())
			.fetch();
	}

	private BooleanExpression filterByMemberId(Long memberId) {
		return inventory.memberId.isNull()
			.or(inventory.memberId.ne(memberId));
	}
}
