package com.moabam.api.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.moabam.api.domain.entity.Product;
import com.moabam.api.domain.repository.ProductRepository;
import com.moabam.api.dto.ProductMapper;
import com.moabam.api.dto.ProductsResponse;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ProductService {

	private final ProductRepository productRepository;

	public ProductsResponse getProducts() {
		List<Product> products = productRepository.findAll();

		return ProductMapper.toProductsResponse(products);
	}
}
