package com.moabam.api.domain.product.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moabam.api.domain.product.Product;
import com.moabam.api.domain.product.ProductType;

public interface ProductRepository extends JpaRepository<Product, Long> {

	List<Product> findAllByType(ProductType type);
}
