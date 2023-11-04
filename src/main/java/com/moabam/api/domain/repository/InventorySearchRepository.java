package com.moabam.api.domain.repository;

import static com.moabam.api.domain.entity.QInventory.*;
import static com.moabam.api.domain.entity.QItem.*;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.moabam.api.domain.entity.Item;
import com.moabam.api.domain.entity.enums.RoomType;
import com.moabam.global.common.util.DynamicQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class InventorySearchRepository {

	private final JPAQueryFactory jpaQueryFactory;

	public List<Item> findItems(Long memberId, RoomType type) {
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
