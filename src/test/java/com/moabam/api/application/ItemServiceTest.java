package com.moabam.api.application;

import static com.moabam.support.fixture.ItemFixture.*;
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

import com.moabam.api.domain.entity.Inventory;
import com.moabam.api.domain.entity.Item;
import com.moabam.api.domain.entity.enums.ItemType;
import com.moabam.api.domain.repository.InventorySearchRepository;
import com.moabam.api.domain.repository.ItemSearchRepository;
import com.moabam.api.dto.ItemResponse;
import com.moabam.api.dto.ItemsResponse;
import com.moabam.global.common.util.StreamUtils;
import com.moabam.global.error.exception.NotFoundException;
import com.moabam.support.fixture.InventoryFixture;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

	@InjectMocks
	ItemService itemService;

	@Mock
	ItemSearchRepository itemSearchRepository;

	@Mock
	InventorySearchRepository inventorySearchRepository;

	@DisplayName("아이템 목록을 조회한다.")
	@Test
	void get_products_success() {
		// given
		Long memberId = 1L;
		ItemType type = ItemType.MORNING;
		Item item1 = morningSantaSkin().build();
		Item item2 = morningKillerSkin().build();
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

	@DisplayName("아이템을 적용한다.")
	@Nested
	class SelectItem {

		@DisplayName("성공한다.")
		@Test
		void success() {
			// given
			Long memberId = 1L;
			Long itemId = 1L;
			Inventory inventory = InventoryFixture.inventory(memberId, nightMageSkin());
			Inventory defaultInventory = InventoryFixture.inventory(memberId, nightMageSkin());
			ItemType itemType = inventory.getItemType();
			given(inventorySearchRepository.findOne(memberId, itemId)).willReturn(Optional.of(inventory));
			given(inventorySearchRepository.findDefault(memberId, itemType)).willReturn(Optional.of(defaultInventory));

			// when
			itemService.selectItem(memberId, itemId);

			// then
			verify(inventorySearchRepository).findOne(memberId, itemId);
			verify(inventorySearchRepository).findDefault(memberId, itemType);
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
