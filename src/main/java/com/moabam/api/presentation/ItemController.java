package com.moabam.api.presentation;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.moabam.api.application.ItemService;
import com.moabam.api.domain.entity.enums.ItemType;
import com.moabam.api.dto.ItemsResponse;
import com.moabam.api.dto.PurchaseItemRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

	private final ItemService itemService;

	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	public ItemsResponse getItems(@RequestParam ItemType type) {
		return itemService.getItems(1L, type);
	}

	@PostMapping("/{itemId}/purchase")
	@ResponseStatus(HttpStatus.OK)
	public void purchaseItem(@PathVariable Long itemId, @Valid @RequestBody PurchaseItemRequest request) {
		itemService.purchaseItem(1L, itemId, request);
	}

	@PostMapping("/{itemId}/select")
	@ResponseStatus(HttpStatus.OK)
	public void selectItem(@PathVariable Long itemId) {
		itemService.selectItem(1L, itemId);
	}
}
