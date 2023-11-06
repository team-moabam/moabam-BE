package com.moabam.api.presentation;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.moabam.api.application.ItemService;
import com.moabam.api.domain.entity.enums.ItemType;
import com.moabam.api.dto.ItemsResponse;

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
}
