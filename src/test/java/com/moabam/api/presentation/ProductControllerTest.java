package com.moabam.api.presentation;

import static java.nio.charset.StandardCharsets.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moabam.api.application.ProductService;
import com.moabam.api.domain.entity.Product;
import com.moabam.api.dto.ProductMapper;
import com.moabam.api.dto.ProductsResponse;
import com.moabam.fixture.ProductFixture;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductControllerTest {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	ObjectMapper objectMapper;

	@MockBean
	ProductService productService;

	@DisplayName("상품 목록을 조회한다.")
	@Test
	void get_products_success() throws Exception {
		// given
		Product product1 = ProductFixture.bugProduct();
		Product product2 = ProductFixture.bugProduct();
		ProductsResponse expected = ProductMapper.toProductsResponse(List.of(product1, product2));
		given(productService.getProducts()).willReturn(expected);

		// when & then
		String content = mockMvc.perform(get("/products"))
			.andDo(print())
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString(UTF_8);
		ProductsResponse actual = objectMapper.readValue(content, ProductsResponse.class);
		Assertions.assertThat(actual).isEqualTo(expected);
	}
}
