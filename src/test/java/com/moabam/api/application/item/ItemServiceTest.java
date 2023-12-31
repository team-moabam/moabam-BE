package com.moabam.api.application.item;

import static com.moabam.support.fixture.InventoryFixture.*;
import static com.moabam.support.fixture.ItemFixture.*;
import static com.moabam.support.fixture.MemberFixture.*;
import static java.util.Collections.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.moabam.api.application.bug.BugService;
import com.moabam.api.application.member.MemberService;
import com.moabam.api.domain.bug.BugType;
import com.moabam.api.domain.bug.repository.BugHistoryRepository;
import com.moabam.api.domain.item.Inventory;
import com.moabam.api.domain.item.Item;
import com.moabam.api.domain.item.ItemType;
import com.moabam.api.domain.item.repository.InventoryRepository;
import com.moabam.api.domain.item.repository.InventorySearchRepository;
import com.moabam.api.domain.item.repository.ItemRepository;
import com.moabam.api.domain.item.repository.ItemSearchRepository;
import com.moabam.api.domain.member.Member;
import com.moabam.api.dto.item.ItemResponse;
import com.moabam.api.dto.item.ItemsResponse;
import com.moabam.api.dto.item.PurchaseItemRequest;
import com.moabam.global.common.util.StreamUtils;
import com.moabam.global.error.exception.ConflictException;
import com.moabam.global.error.exception.NotFoundException;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

	@InjectMocks
	ItemService itemService;

	@Mock
	MemberService memberService;

	@Mock
	BugService bugService;

	@Mock
	ItemRepository itemRepository;

	@Mock
	ItemSearchRepository itemSearchRepository;

	@Mock
	InventoryRepository inventoryRepository;

	@Mock
	InventorySearchRepository inventorySearchRepository;

	@Mock
	BugHistoryRepository bugHistoryRepository;

	@DisplayName("아이템 목록을 조회한다.")
	@Test
	void get_products_success() {
		// given
		Long memberId = 1L;
		ItemType type = ItemType.MORNING;
		Item item1 = morningSantaSkin().build();
		Item item2 = morningKillerSkin().build();
		Inventory inventory = inventory(memberId, item1);
		given(inventorySearchRepository.findDefault(memberId, type)).willReturn(Optional.of(inventory));
		given(inventorySearchRepository.findItems(memberId, type)).willReturn(List.of(item1, item2));
		given(itemSearchRepository.findNotPurchasedItems(memberId, type)).willReturn(emptyList());

		// when
		ItemsResponse response = itemService.getItems(memberId, type);

		// then
		List<String> purchasedItemNames = StreamUtils.map(response.purchasedItems(), ItemResponse::name);
		assertThat(response.purchasedItems()).hasSize(2);
		assertThat(purchasedItemNames).containsExactly(MORNING_SANTA_SKIN_NAME, MORNING_KILLER_SKIN_NAME);
		assertThat(response.notPurchasedItems()).isEmpty();
	}

	@DisplayName("아이템을 구매한다.")
	@Nested
	class PurchaseItem {

		@DisplayName("성공한다.")
		@Test
		void success() {
			// given
			Long memberId = 1L;
			Long itemId = 1L;
			PurchaseItemRequest request = new PurchaseItemRequest(BugType.GOLDEN);
			Member member = member();
			Item item = nightMageSkin();
			given(memberService.findMember(memberId)).willReturn(member);
			given(itemRepository.findById(itemId)).willReturn(Optional.of(item));
			given(inventorySearchRepository.findOne(memberId, itemId)).willReturn(Optional.empty());

			// When
			itemService.purchaseItem(memberId, itemId, request);

			// Then
			verify(bugService).use(any(Member.class), any(BugType.class), anyInt());
			verify(inventoryRepository).save(any(Inventory.class));
		}

		@DisplayName("해당 아이템이 존재하지 않으면 예외가 발생한다.")
		@Test
		void item_not_found_exception() {
			// given
			Long memberId = 1L;
			Long itemId = 1L;
			PurchaseItemRequest request = new PurchaseItemRequest(BugType.GOLDEN);
			given(itemRepository.findById(itemId)).willReturn(Optional.empty());

			// when, then
			assertThatThrownBy(() -> itemService.purchaseItem(memberId, itemId, request))
				.isInstanceOf(NotFoundException.class)
				.hasMessage("존재하지 않는 아이템입니다.");
		}

		@DisplayName("이미 구매한 아이템이면 예외가 발생한다.")
		@Test
		void inventory_conflict_exception() {
			// given
			Long memberId = 1L;
			Long itemId = 1L;
			PurchaseItemRequest request = new PurchaseItemRequest(BugType.GOLDEN);
			Item item = nightMageSkin();
			Inventory inventory = inventory(memberId, item);
			given(itemRepository.findById(itemId)).willReturn(Optional.of(item));
			given(inventorySearchRepository.findOne(memberId, itemId)).willReturn(Optional.of(inventory));

			// when, then
			assertThatThrownBy(() -> itemService.purchaseItem(memberId, itemId, request))
				.isInstanceOf(ConflictException.class)
				.hasMessage("이미 구매한 아이템입니다.");
		}
	}

	@DisplayName("아이템을 적용한다.")
	@Nested
	class SelectItem {

		@DisplayName("성공한다.")
		@Test
		void success() {
			// given
			Long memberId = 1L;
			Long itemId = 1L;
			Inventory inventory = inventory(memberId, nightMageSkin());
			Inventory defaultInventory = inventory(memberId, nightMageSkin());
			ItemType itemType = inventory.getItemType();
			given(memberService.findMember(memberId)).willReturn(member());
			given(inventorySearchRepository.findOne(memberId, itemId)).willReturn(Optional.of(inventory));
			given(inventorySearchRepository.findDefault(memberId, itemType)).willReturn(Optional.of(defaultInventory));

			// when
			itemService.selectItem(memberId, itemId);

			// then
			assertFalse(defaultInventory.isDefault());
			assertTrue(inventory.isDefault());
		}

		@DisplayName("인벤토리 아이템이 아니면 예외가 발생한다.")
		@Test
		void exception() {
			// given
			Long memberId = 1L;
			Long itemId = 1L;
			given(inventorySearchRepository.findOne(memberId, itemId)).willReturn(Optional.empty());

			// when, then
			assertThatThrownBy(() -> itemService.selectItem(memberId, itemId))
				.isInstanceOf(NotFoundException.class)
				.hasMessage("구매하지 않은 아이템은 적용할 수 없습니다.");
		}
	}
}
