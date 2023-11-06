package com.moabam.api.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moabam.api.domain.entity.Inventory;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

}
