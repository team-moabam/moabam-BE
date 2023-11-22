package com.moabam.api.application.item;

import static com.moabam.global.error.model.ErrorMessage.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.moabam.api.domain.item.Inventory;
import com.moabam.api.domain.item.Item;
import com.moabam.api.domain.item.repository.InventorySearchRepository;
import com.moabam.global.error.exception.BadRequestException;
import com.moabam.support.fixture.InventoryFixture;
import com.moabam.support.fixture.ItemFixture;

@ExtendWith(MockitoExtension.class)
public class InventorySearchServiceTest {

	@InjectMocks
	InventorySearchService inventorySearchService;

	@Mock
	InventorySearchRepository inventorySearchRepository;

	@DisplayName("기본 스킨을 가져온다.")
	@Nested
	class GetDefaultSkin {

		@DisplayName("성공")
		@Test
		void success() {
			// given
			long searchId = 1L;
			Item morning = ItemFixture.morningSantaSkin().build();
			Item night = ItemFixture.nightMageSkin();
			Inventory morningSkin = InventoryFixture.inventory(searchId, morning);
			Inventory nightSkin = InventoryFixture.inventory(searchId, night);

			given(inventorySearchRepository.findBirdsDefaultSkin(searchId)).willReturn(List.of(morningSkin, nightSkin));

			// when
			List<Inventory> inventories = inventorySearchService.getDefaultSkin(1L);

			// then
			assertThat(inventories).contains(morningSkin, nightSkin);
		}

		@DisplayName("기본 스킨이 없어서 예외 발생")
		@Test
		void failBy_underSize() {
			// given
			long searchId = 1L;

			given(inventorySearchRepository.findBirdsDefaultSkin(searchId)).willReturn(List.of());

			// when
			assertThatThrownBy(() -> inventorySearchService.getDefaultSkin(1L))
				.isInstanceOf(BadRequestException.class)
				.hasMessage(INVALID_DEFAULT_SKIN_SIZE.getMessage());
		}

		@DisplayName("기본 스킨이 3개 이상이어서 예외 발생")
		@Test
		void failBy_overSize() {
			// given
			long searchId = 1L;
			Item morning = ItemFixture.morningSantaSkin().build();
			Item night = ItemFixture.nightMageSkin();
			Item kill = ItemFixture.morningKillerSkin().build();
			Inventory morningSkin = InventoryFixture.inventory(searchId, morning);
			Inventory nightSkin = InventoryFixture.inventory(searchId, night);
			Inventory killSkin = InventoryFixture.inventory(searchId, kill);

			given(inventorySearchRepository.findBirdsDefaultSkin(searchId))
				.willReturn(List.of(morningSkin, nightSkin, killSkin));

			// when
			assertThatThrownBy(() -> inventorySearchService.getDefaultSkin(1L))
				.isInstanceOf(BadRequestException.class)
				.hasMessage(INVALID_DEFAULT_SKIN_SIZE.getMessage());
		}
	}
}
