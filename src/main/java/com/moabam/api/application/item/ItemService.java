package com.moabam.api.application.item;

import static com.moabam.global.error.model.ErrorMessage.*;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.moabam.api.application.bug.BugMapper;
import com.moabam.api.application.member.MemberService;
import com.moabam.api.domain.bug.Bug;
import com.moabam.api.domain.bug.repository.BugHistoryRepository;
import com.moabam.api.domain.item.Inventory;
import com.moabam.api.domain.item.Item;
import com.moabam.api.domain.item.ItemType;
import com.moabam.api.domain.item.repository.InventoryRepository;
import com.moabam.api.domain.item.repository.InventorySearchRepository;
import com.moabam.api.domain.item.repository.ItemRepository;
import com.moabam.api.domain.item.repository.ItemSearchRepository;
import com.moabam.api.domain.member.Member;
import com.moabam.api.dto.item.ItemsResponse;
import com.moabam.api.dto.item.PurchaseItemRequest;
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
		Item defaultItem = getDefaultInventory(memberId, type).getItem();
		List<Item> purchasedItems = inventorySearchRepository.findItems(memberId, type);
		List<Item> notPurchasedItems = itemSearchRepository.findNotPurchasedItems(memberId, type);

		return ItemMapper.toItemsResponse(defaultItem.getId(), purchasedItems, notPurchasedItems);
	}

	@Transactional
	public void purchaseItem(Long memberId, Long itemId, PurchaseItemRequest request) {
		Item item = getItem(itemId);
		Member member = memberService.findMember(memberId);

		validateAlreadyPurchased(memberId, itemId);
		item.validatePurchasable(request.bugType(), member.getLevel());

		Bug bug = member.getBug();
		int price = item.getPrice(request.bugType());

		bug.use(request.bugType(), price);
		inventoryRepository.save(ItemMapper.toInventory(memberId, item));
		bugHistoryRepository.save(BugMapper.toUseBugHistory(memberId, request.bugType(), price));
	}

	@Transactional
	public void selectItem(Long memberId, Long itemId) {
		Inventory inventory = getInventory(memberId, itemId);

		inventorySearchRepository.findDefault(memberId, inventory.getItemType())
			.ifPresent(Inventory::deselect);
		inventory.select();
	}

	private Item getItem(Long itemId) {
		return itemRepository.findById(itemId)
			.orElseThrow(() -> new NotFoundException(ITEM_NOT_FOUND));
	}

	private Inventory getInventory(Long memberId, Long itemId) {
		return inventorySearchRepository.findOne(memberId, itemId)
			.orElseThrow(() -> new NotFoundException(INVENTORY_NOT_FOUND));
	}

	private Inventory getDefaultInventory(Long memberId, ItemType type) {
		return inventorySearchRepository.findDefault(memberId, type)
			.orElseThrow(() -> new NotFoundException(DEFAULT_INVENTORY_NOT_FOUND));
	}

	private void validateAlreadyPurchased(Long memberId, Long itemId) {
		inventorySearchRepository.findOne(memberId, itemId)
			.ifPresent(inventory -> {
				throw new ConflictException(INVENTORY_CONFLICT);
			});
	}
}
