package com.moabam.api.domain.item.repository;

import static com.moabam.support.fixture.InventoryFixture.*;
import static com.moabam.support.fixture.ItemFixture.*;
import static com.moabam.support.fixture.MemberFixture.*;
import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.moabam.api.domain.item.Inventory;
import com.moabam.api.domain.item.Item;
import com.moabam.api.domain.item.ItemType;
import com.moabam.api.domain.member.Member;
import com.moabam.api.infrastructure.repository.member.MemberRepository;
import com.moabam.support.annotation.QuerydslRepositoryTest;

@QuerydslRepositoryTest
class InventorySearchRepositoryTest {

	@Autowired
	InventorySearchRepository inventorySearchRepository;

	@Autowired
	MemberRepository memberRepository;

	@Autowired
	ItemRepository itemRepository;

	@Autowired
	InventoryRepository inventoryRepository;

	@DisplayName("인벤토리 아이템 목록을 조회한다.")
	@Nested
	class FindItems {

		@DisplayName("해당 타입의 아이템 목록을 구매일 순으로 정렬한다.")
		@Test
		void sorted_by_created_at_success() {
			// given
			Long memberId = 1L;
			Item morningSantaSkin = itemRepository.save(morningSantaSkin().build());
			inventoryRepository.save(inventory(memberId, morningSantaSkin));
			Item morningKillerSkin = itemRepository.save(morningKillerSkin().build());
			inventoryRepository.save(inventory(memberId, morningKillerSkin));
			Item nightMageSkin = itemRepository.save(nightMageSkin());
			inventoryRepository.save(inventory(memberId, nightMageSkin));

			// when
			List<Item> actual = inventorySearchRepository.findItems(memberId, ItemType.MORNING);

			// then
			assertThat(actual).hasSize(2)
				.containsExactly(morningKillerSkin, morningSantaSkin);
		}

		@DisplayName("해당 타입의 아이템이 없으면 빈 목록을 조회한다.")
		@Test
		void empty_success() {
			// given
			Long memberId = 1L;
			Item morningSantaSkin = itemRepository.save(morningSantaSkin().build());
			inventoryRepository.save(inventory(memberId, morningSantaSkin));
			Item morningKillerSkin = itemRepository.save(morningKillerSkin().build());
			inventoryRepository.save(inventory(memberId, morningKillerSkin));

			// when
			List<Item> actual = inventorySearchRepository.findItems(memberId, ItemType.NIGHT);

			// then
			assertThat(actual).isEmpty();
		}
	}

	@DisplayName("인벤토리를 조회한다.")
	@Test
	void find_one_success() {
		// given
		Member member = memberRepository.save(member());
		Item item = itemRepository.save(nightMageSkin());
		Inventory inventory = inventoryRepository.save(inventory(member.getId(), item));

		// when
		Optional<Inventory> actual = inventorySearchRepository.findOne(member.getId(), item.getId());

		// then
		assertThat(actual).isPresent().contains(inventory);
	}

	@DisplayName("현재 적용된 인벤토리를 조회한다.")
	@Test
	void find_default_success() {
		// given
		Member member = memberRepository.save(member());
		Item item = itemRepository.save(nightMageSkin());
		Inventory inventory = inventoryRepository.save(inventory(member.getId(), item));
		inventory.select();

		// when
		Optional<Inventory> actual = inventorySearchRepository.findDefault(member.getId(), inventory.getItemType());

		// then
		assertThat(actual).isPresent().contains(inventory);
	}
}
