package com.moabam.api.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moabam.api.domain.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

}
