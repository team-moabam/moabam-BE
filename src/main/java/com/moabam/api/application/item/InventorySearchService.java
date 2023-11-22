package com.moabam.api.application.item;

import static com.moabam.global.error.model.ErrorMessage.*;

import java.util.List;

import org.springframework.stereotype.Service;

import com.moabam.api.domain.item.Inventory;
import com.moabam.api.domain.item.repository.InventorySearchRepository;
import com.moabam.global.common.util.GlobalConstant;
import com.moabam.global.error.exception.BadRequestException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class InventorySearchService {

	private final InventorySearchRepository inventorySearchRepository;

	public List<Inventory> getDefaultSkin(Long searchId) {
		List<Inventory> inventories = inventorySearchRepository.findBirdsDefaultSkin(searchId);
		if (inventories.size() != GlobalConstant.DEFAULT_SKIN_SIZE) {
			throw new BadRequestException(INVALID_DEFAULT_SKIN_SIZE);
		}

		return inventories;
	}
}
