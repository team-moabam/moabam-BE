package com.moabam.api.presentation;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.moabam.api.application.ItemService;
import com.moabam.api.domain.entity.enums.RoomType;
import com.moabam.api.dto.ItemsResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

	private final ItemService itemService;

	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	public ItemsResponse getItems(@RequestParam RoomType type) {
		return itemService.getItems(1L, type);
	}

	@PostMapping("/{itemId}/select")
	@ResponseStatus(HttpStatus.OK)
	public void selectItem(@PathVariable Long itemId) {
		itemService.selectItem(1L, itemId);
	}
}
