package com.moabam.api.application;

import static com.moabam.global.error.model.ErrorMessage.*;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.moabam.api.domain.entity.Inventory;
import com.moabam.api.domain.entity.Item;
import com.moabam.api.domain.entity.enums.RoomType;
import com.moabam.api.domain.repository.InventorySearchRepository;
import com.moabam.api.domain.repository.ItemSearchRepository;
import com.moabam.api.dto.ItemMapper;
import com.moabam.api.dto.ItemsResponse;
import com.moabam.global.error.exception.NotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemService {

	private final ItemSearchRepository itemSearchRepository;
	private final InventorySearchRepository inventorySearchRepository;

	public ItemsResponse getItems(Long memberId, RoomType type) {
		List<Item> purchasedItems = inventorySearchRepository.findItems(memberId, type);
		List<Item> notPurchasedItems = itemSearchRepository.findNotPurchasedItems(memberId, type);

		return ItemMapper.toItemsResponse(purchasedItems, notPurchasedItems);
	}

	public void selectItem(Long memberId, Long itemId) {
		Inventory inventory = getInventory(memberId, itemId);

		inventorySearchRepository.findDefault(memberId)
			.ifPresent(Inventory::unsetDefault);
		inventory.setDefault();
	}

	private Inventory getInventory(Long memberId, Long itemId) {
		return inventorySearchRepository.findOne(memberId, itemId)
			.orElseThrow(() -> new NotFoundException(INVENTORY_NOT_FOUND));
	}
}
