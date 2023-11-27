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
import com.moabam.api.domain.member.repository.MemberRepository;
import com.moabam.api.domain.room.RoomType;
import com.moabam.support.annotation.QuerydslRepositoryTest;
import com.moabam.support.fixture.InventoryFixture;
import com.moabam.support.fixture.ItemFixture;
import com.moabam.support.fixture.MemberFixture;

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
		Member member = memberRepository.save(member("999", "test"));
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
		Member member = memberRepository.save(member("11314", "test"));
		Item item = itemRepository.save(nightMageSkin());
		Inventory inventory = inventoryRepository.save(inventory(member.getId(), item));
		inventory.select();

		// when
		Optional<Inventory> actual = inventorySearchRepository.findDefault(member.getId(), inventory.getItemType());

		// then
		assertThat(actual).isPresent().contains(inventory);
	}

	@DisplayName("여러 회원의 밤 타입에 적용된 인벤토리를 조회한다.")
	@Test
	void find_all_default_type_night_success() {
		// given
		Member member1 = memberRepository.save(member("625", "회원1"));
		Member member2 = memberRepository.save(member("255", "회원2"));
		Item item = itemRepository.save(nightMageSkin());
		Inventory inventory1 = inventoryRepository.save(inventory(member1.getId(), item));
		Inventory inventory2 = inventoryRepository.save(inventory(member2.getId(), item));
		inventory1.select();
		inventory2.select();

		// when
		List<Inventory> actual = inventorySearchRepository.findDefaultInventories(List.of(member1.getId(),
			member2.getId()), RoomType.NIGHT.name());

		// then
		assertThat(actual).hasSize(2);
		assertThat(actual.get(0).getItem().getName()).isEqualTo(nightMageSkin().getName());
	}

	@DisplayName("기본 새 찾는 쿼리")
	@Nested
	class FindDefaultBird {

		@DisplayName("default 가져오기 성공")
		@Test
		void bird_find_success() {
			// given
			Member member = MemberFixture.member("fffdd", "test");
			member.exitRoom(RoomType.MORNING);
			memberRepository.save(member);

			Item night = ItemFixture.nightMageSkin();
			Item morning = ItemFixture.morningSantaSkin().build();
			Item killer = ItemFixture.morningKillerSkin().build();
			itemRepository.saveAll(List.of(night, morning, killer));

			Inventory nightInven = InventoryFixture.inventory(member.getId(), night);
			nightInven.select();

			Inventory morningInven = InventoryFixture.inventory(member.getId(), morning);
			morningInven.select();

			Inventory killerInven = InventoryFixture.inventory(member.getId(), killer);
			inventoryRepository.saveAll(List.of(nightInven, morningInven, killerInven));

			// when
			List<Inventory> inventories = inventorySearchRepository.findDefaultSkin(member.getId());

			// then
			assertThat(inventories).hasSize(2);
		}
	}
}
