package com.moabam.api.application;

import static com.moabam.global.error.model.ErrorMessage.*;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.moabam.api.domain.entity.BugHistory;
import com.moabam.api.domain.entity.Inventory;
import com.moabam.api.domain.entity.Item;
import com.moabam.api.domain.entity.Member;
import com.moabam.api.domain.entity.Wallet;
import com.moabam.api.domain.entity.enums.ItemType;
import com.moabam.api.domain.repository.BugHistoryRepository;
import com.moabam.api.domain.repository.InventoryRepository;
import com.moabam.api.domain.repository.InventorySearchRepository;
import com.moabam.api.domain.repository.ItemRepository;
import com.moabam.api.domain.repository.ItemSearchRepository;
import com.moabam.api.dto.ItemMapper;
import com.moabam.api.dto.ItemsResponse;
import com.moabam.api.dto.PurchaseItemRequest;
import com.moabam.global.error.exception.ConflictException;
import com.moabam.global.error.exception.NotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemService {

	private final MemberService memberService;
	private final ItemRepository itemRepository;
	private final ItemSearchRepository itemSearchRepository;
	private final InventoryRepository inventoryRepository;
	private final InventorySearchRepository inventorySearchRepository;
	private final BugHistoryRepository bugHistoryRepository;

	public ItemsResponse getItems(Long memberId, ItemType type) {
		List<Item> purchasedItems = inventorySearchRepository.findItems(memberId, type);
		List<Item> notPurchasedItems = itemSearchRepository.findNotPurchasedItems(memberId, type);

		return ItemMapper.toItemsResponse(purchasedItems, notPurchasedItems);
	}

	@Transactional
	public void purchaseItem(Long memberId, Long itemId, PurchaseItemRequest request) {
		Item item = getItem(itemId);
		Member member = memberService.getById(memberId);

		validateAlreadyPurchased(memberId, itemId);
		item.validatePurchasable(request.bugType(), member.getLevel());

		Wallet wallet = member.getWallet();
		int price = item.getPrice(request.bugType());

		wallet.use(request.bugType(), price);
		inventoryRepository.save(Inventory.create(memberId, item));
		bugHistoryRepository.save(BugHistory.createUseBugHistory(memberId, request.bugType(), price));
	}

	@Transactional
	public void selectItem(Long memberId, Long itemId) {
		Inventory inventory = getInventory(memberId, itemId);

		inventorySearchRepository.findDefault(memberId, inventory.getItemType())
			.ifPresent(Inventory::unsetDefault);
		inventory.setDefault();
	}

	private Inventory getInventory(Long memberId, Long itemId) {
		return inventorySearchRepository.findOne(memberId, itemId)
			.orElseThrow(() -> new NotFoundException(INVENTORY_NOT_FOUND));
	}

	private Item getItem(Long itemId) {
		return itemRepository.findById(itemId)
			.orElseThrow(() -> new NotFoundException(ITEM_NOT_FOUND));
	}

	private void validateAlreadyPurchased(Long memberId, Long itemId) {
		inventorySearchRepository.findOne(memberId, itemId)
			.ifPresent(inventory -> {
				throw new ConflictException(INVENTORY_CONFLICT);
			});
	}
}
