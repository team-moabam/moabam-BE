package com.moabam.api.application.product;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.moabam.api.domain.product.Product;
import com.moabam.api.domain.product.repository.ProductRepository;
import com.moabam.api.dto.product.ProductsResponse;

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
