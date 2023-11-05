package com.moabam.api.domain.repository;

import static com.moabam.support.fixture.InventoryFixture.*;
import static com.moabam.support.fixture.ItemFixture.*;
import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.moabam.api.domain.entity.Item;
import com.moabam.api.domain.entity.enums.RoomType;
import com.moabam.support.annotation.RepositoryTest;

@RepositoryTest
class ItemSearchRepositoryTest {

	@Autowired
	ItemSearchRepository itemSearchRepository;

	@Autowired
	ItemRepository itemRepository;

	@Autowired
	InventoryRepository inventoryRepository;

	@DisplayName("타입으로 구매하지 않은 아이템 목록을 조회한다.")
	@Test
	void find_not_purchased_items_success() {
		// given
		Long memberId = 1L;
		Item morningSantaSkin = itemRepository.save(morningSantaSkin().build());
		inventoryRepository.save(inventory(memberId, morningSantaSkin));
		Item morningKillerSkin = itemRepository.save(morningKillerSkin().build());
		itemRepository.save(nightMageSkin());

		// when
		List<Item> actual = itemSearchRepository.findNotPurchasedItems(memberId, RoomType.MORNING);

		// then
		assertThat(actual).hasSize(1)
			.containsExactly(morningKillerSkin);
	}

	@DisplayName("구매하지 않은 아이템 목록은 레벨 순으로 정렬된다.")
	@Test
	void find_not_purchased_items_sorted_by_level_success() {
		// given
		Long memberId = 1L;
		Item morningSantaSkin = itemRepository.save(morningSantaSkin().unlockLevel(5).build());
		Item morningKillerSkin = itemRepository.save(morningKillerSkin().unlockLevel(1).build());

		// when
		List<Item> actual = itemSearchRepository.findNotPurchasedItems(memberId, RoomType.MORNING);

		// then
		assertThat(actual).hasSize(2)
			.containsExactly(morningKillerSkin, morningSantaSkin);
	}

	@DisplayName("레벨이 같으면 가격 순으로 정렬된다.")
	@Test
	void find_not_purchased_items_sorted_by_price_success() {
		// given
		Long memberId = 1L;
		Item morningSantaSkin = itemRepository.save(morningSantaSkin().bugPrice(10).build());
		Item morningKillerSkin = itemRepository.save(morningKillerSkin().bugPrice(20).build());

		// when
		List<Item> actual = itemSearchRepository.findNotPurchasedItems(memberId, RoomType.MORNING);

		// then
		assertThat(actual).hasSize(2)
			.containsExactly(morningSantaSkin, morningKillerSkin);
	}

	@DisplayName("레벨과 가격이 같으면 이름 순으로 정렬된다.")
	@Test
	void find_not_purchased_items_sorted_by_name_success() {
		// given
		Long memberId = 1L;
		Item morningSantaSkin = itemRepository.save(morningSantaSkin().build());
		Item morningKillerSkin = itemRepository.save(morningKillerSkin().build());

		// when
		List<Item> actual = itemSearchRepository.findNotPurchasedItems(memberId, RoomType.MORNING);

		// then
		assertThat(actual).hasSize(2)
			.containsExactly(morningSantaSkin, morningKillerSkin);
	}
}
