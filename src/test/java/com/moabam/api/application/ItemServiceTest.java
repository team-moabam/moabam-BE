package com.moabam.api.application;

import static com.moabam.fixture.ItemFixture.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.moabam.api.domain.entity.Item;
import com.moabam.api.domain.entity.enums.RoomType;
import com.moabam.api.domain.repository.InventorySearchRepository;
import com.moabam.api.domain.repository.ItemSearchRepository;
import com.moabam.api.dto.ItemResponse;
import com.moabam.api.dto.ItemsResponse;

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
		RoomType type = RoomType.MORNING;
		Item item1 = morningSantaSkin().build();
		Item item2 = morningKillerSkin().build();
		given(inventorySearchRepository.findItems(memberId, type)).willReturn(List.of(item1, item2));
		given(itemSearchRepository.findNotPurchasedItems(memberId, type)).willReturn(List.of(item1, item2));

		// when
		ItemsResponse response = itemService.getItems(memberId, type);

		// then
		List<String> purchasedItemNames = response.purchasedItems().stream()
			.map(ItemResponse::name)
			.toList();
		List<String> notPurchasedItemNames = response.notPurchasedItems().stream()
			.map(ItemResponse::name)
			.toList();
		assertThat(response.purchasedItems()).hasSize(2);
		assertThat(response.notPurchasedItems()).hasSize(2);
		assertThat(purchasedItemNames).containsExactly(MORNING_SANTA_SKIN_NAME, MORNING_KILLER_SKIN_NAME);
		assertThat(notPurchasedItemNames).containsExactly(MORNING_SANTA_SKIN_NAME, MORNING_KILLER_SKIN_NAME);
	}
}
