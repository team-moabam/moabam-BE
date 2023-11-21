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

import com.moabam.api.application.item.ItemService;
import com.moabam.api.domain.item.ItemType;
import com.moabam.api.dto.item.ItemsResponse;
import com.moabam.api.dto.item.PurchaseItemRequest;
import com.moabam.global.auth.annotation.Auth;
import com.moabam.global.auth.model.AuthMember;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

	private final ItemService itemService;

	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	public ItemsResponse getItems(@Auth AuthMember member, @RequestParam ItemType type) {
		return itemService.getItems(member.id(), type);
	}

	@PostMapping("/{itemId}/purchase")
	@ResponseStatus(HttpStatus.OK)
	public void purchaseItem(@Auth AuthMember member,
		@PathVariable Long itemId,
		@Valid @RequestBody PurchaseItemRequest request) {
		itemService.purchaseItem(member.id(), itemId, request);
	}

	@PostMapping("/{itemId}/select")
	@ResponseStatus(HttpStatus.OK)
	public void selectItem(@Auth AuthMember member, @PathVariable Long itemId) {
		itemService.selectItem(member.id(), itemId);
	}
}
