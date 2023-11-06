package com.moabam.api.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moabam.api.domain.entity.Item;

public interface ItemRepository extends JpaRepository<Item, Long> {

}
