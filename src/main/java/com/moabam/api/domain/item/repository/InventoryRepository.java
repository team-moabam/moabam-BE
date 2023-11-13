package com.moabam.api.domain.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moabam.api.domain.item.Inventory;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

}
