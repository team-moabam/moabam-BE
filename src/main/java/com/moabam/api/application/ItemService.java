package com.moabam.api.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.moabam.api.domain.entity.Item;
import com.moabam.api.domain.entity.enums.ItemType;
import com.moabam.api.domain.repository.InventorySearchRepository;
import com.moabam.api.domain.repository.ItemSearchRepository;
import com.moabam.api.dto.ItemMapper;
import com.moabam.api.dto.ItemsResponse;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemService {

	private final ItemSearchRepository itemSearchRepository;
	private final InventorySearchRepository inventorySearchRepository;

	public ItemsResponse getItems(Long memberId, ItemType type) {
		List<Item> purchasedItems = inventorySearchRepository.findItems(memberId, type);
		List<Item> notPurchasedItems = itemSearchRepository.findNotPurchasedItems(memberId, type);

		return ItemMapper.toItemsResponse(purchasedItems, notPurchasedItems);
	}
}
