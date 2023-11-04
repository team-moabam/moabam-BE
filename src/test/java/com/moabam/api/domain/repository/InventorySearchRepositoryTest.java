package com.moabam.api.domain.repository;

import static com.moabam.fixture.InventoryFixture.*;
import static com.moabam.fixture.ItemFixture.*;
import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.moabam.api.domain.entity.Item;
import com.moabam.api.domain.entity.enums.RoomType;

@SpringBootTest
@Transactional(readOnly = true)
class InventorySearchRepositoryTest {

	@Autowired
	ItemRepository itemRepository;

	@Autowired
	InventoryRepository inventoryRepository;

	@Autowired
	InventorySearchRepository inventorySearchRepository;

	@DisplayName("타입으로 인벤토리에 있는 아이템 목록을 구매일 순으로 조회한다.")
	@Test
	void find_items_success() {
		// given
		Long memberId = 1L;
		Item morningSantaSkin = itemRepository.save(morningSantaSkin().build());
		inventoryRepository.save(inventory(memberId, morningSantaSkin));
		Item morningKillerSkin = itemRepository.save(morningKillerSkin().build());
		inventoryRepository.save(inventory(memberId, morningKillerSkin));
		Item nightMageSkin = itemRepository.save(nightMageSkin());
		inventoryRepository.save(inventory(memberId, nightMageSkin));

		// when
		List<Item> actual = inventorySearchRepository.findItems(memberId, RoomType.MORNING);

		// then
		assertThat(actual).hasSize(2)
			.containsExactly(morningKillerSkin, morningSantaSkin);
	}

	@DisplayName("인벤토리에 해당하는 타입의 아이템이 없으면 빈 목록을 조회한다.")
	@Test
	void find_empty_success() {
		// given
		Long memberId = 1L;
		Item morningSantaSkin = itemRepository.save(morningSantaSkin().build());
		inventoryRepository.save(inventory(memberId, morningSantaSkin));
		Item morningKillerSkin = itemRepository.save(morningKillerSkin().build());
		inventoryRepository.save(inventory(memberId, morningKillerSkin));

		// when
		List<Item> actual = inventorySearchRepository.findItems(memberId, RoomType.NIGHT);

		// then
		assertThat(actual).isEmpty();
	}
}
