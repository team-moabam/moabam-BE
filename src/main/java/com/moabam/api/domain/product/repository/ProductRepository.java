package com.moabam.api.domain.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moabam.api.domain.product.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

}
