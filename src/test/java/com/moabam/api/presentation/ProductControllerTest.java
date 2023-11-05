package com.moabam.api.presentation;

import static com.moabam.fixture.ProductFixture.*;
import static java.nio.charset.StandardCharsets.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.http.MediaType.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moabam.api.application.ProductService;
import com.moabam.api.domain.entity.Product;
import com.moabam.api.dto.ProductMapper;
import com.moabam.api.dto.ProductsResponse;

@WebMvcTest(ProductController.class)
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
		Product product1 = bugProduct();
		Product product2 = bugProduct();
		ProductsResponse expected = ProductMapper.toProductsResponse(List.of(product1, product2));
		given(productService.getProducts()).willReturn(expected);

		// when, then
		String content = mockMvc.perform(get("/products")
				.contentType(APPLICATION_JSON))
			.andDo(print())
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString(UTF_8);
		ProductsResponse actual = objectMapper.readValue(content, ProductsResponse.class);
		assertThat(actual).isEqualTo(expected);
	}
}
