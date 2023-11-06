package com.moabam.api.application;

import static com.moabam.fixture.ProductFixture.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.moabam.api.domain.entity.Product;
import com.moabam.api.domain.repository.ProductRepository;
import com.moabam.api.dto.ProductResponse;
import com.moabam.api.dto.ProductsResponse;
import com.moabam.global.common.util.StreamUtils;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

	@InjectMocks
	ProductService productService;

	@Mock
	ProductRepository productRepository;

	@DisplayName("상품 목록을 조회한다.")
	@Test
	void get_products_success() {
		// given
		Product product1 = bugProduct();
		Product product2 = bugProduct();
		given(productRepository.findAll()).willReturn(List.of(product1, product2));

		// when
		ProductsResponse response = productService.getProducts();

		// then
		List<String> productNames = StreamUtils.map(response.products(), ProductResponse::name);
		assertThat(response.products()).hasSize(2);
		assertThat(productNames).containsExactly(BUG_PRODUCT_NAME, BUG_PRODUCT_NAME);
	}
}
