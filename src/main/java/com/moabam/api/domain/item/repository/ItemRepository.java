package com.moabam.api.domain.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moabam.api.domain.item.Item;

public interface ItemRepository extends JpaRepository<Item, Long> {

}
