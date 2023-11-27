package com.moabam.api.presentation;

import static com.moabam.global.auth.model.AuthorizationThreadLocal.*;
import static com.moabam.support.fixture.BugFixture.*;
import static com.moabam.support.fixture.MemberFixture.*;
import static com.moabam.support.fixture.PaymentFixture.*;
import static com.moabam.support.fixture.ProductFixture.*;
import static java.nio.charset.StandardCharsets.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.http.MediaType.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moabam.api.application.bug.BugMapper;
import com.moabam.api.application.member.MemberService;
import com.moabam.api.application.product.ProductMapper;
import com.moabam.api.domain.bug.BugActionType;
import com.moabam.api.domain.bug.BugType;
import com.moabam.api.domain.bug.repository.BugHistoryRepository;
import com.moabam.api.domain.bug.repository.BugHistorySearchRepository;
import com.moabam.api.domain.payment.Payment;
import com.moabam.api.domain.payment.repository.PaymentRepository;
import com.moabam.api.domain.product.Product;
import com.moabam.api.domain.product.repository.ProductRepository;
import com.moabam.api.dto.bug.BugHistoryResponse;
import com.moabam.api.dto.bug.BugResponse;
import com.moabam.api.dto.product.ProductsResponse;
import com.moabam.api.dto.product.PurchaseProductRequest;
import com.moabam.api.dto.product.PurchaseProductResponse;
import com.moabam.support.annotation.WithMember;
import com.moabam.support.common.WithoutFilterSupporter;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class BugControllerTest extends WithoutFilterSupporter {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	ObjectMapper objectMapper;

	@MockBean
	MemberService memberService;

	@Autowired
	BugHistoryRepository bugHistoryRepository;

	@Autowired
	BugHistorySearchRepository bugHistorySearchRepository;

	@Autowired
	ProductRepository productRepository;

	@Autowired
	PaymentRepository paymentRepository;

	@DisplayName("벌레를 조회한다.")
	@WithMember
	@Test
	void get_bug_success() throws Exception {
		// given
		Long memberId = getAuthMember().id();
		BugResponse expected = BugMapper.toBugResponse(bug());
		given(memberService.getById(memberId)).willReturn(member());

		// expected
		String content = mockMvc.perform(get("/bugs")
				.contentType(APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(print())
			.andReturn()
			.getResponse()
			.getContentAsString(UTF_8);
		BugResponse actual = objectMapper.readValue(content, BugResponse.class);
		assertThat(actual).isEqualTo(expected);
	}

	@DisplayName("벌레 내역을 조회한다.")
	@Nested
	class GetBugHistory {

		@DisplayName("성공한다.")
		@WithMember
		@Test
		void success() throws Exception {
			// given
			Long memberId = getAuthMember().id();
			bugHistoryRepository.save(rewardMorningBugHistory(memberId));

			// expected
			String content = mockMvc.perform(get("/bugs/history")
					.contentType(APPLICATION_JSON))
				.andExpect(status().isOk())
				.andDo(print())
				.andReturn()
				.getResponse()
				.getContentAsString(UTF_8);
			BugHistoryResponse actual = objectMapper.readValue(content, BugHistoryResponse.class);
			assertThat(actual.history().get(0).bugType()).isEqualTo(BugType.MORNING);
			assertThat(actual.history().get(0).actionType()).isEqualTo(BugActionType.REWARD);
			assertThat(actual.history().get(0).quantity()).isEqualTo(REWARD_MORNING_BUG);
			assertThat(actual.history().get(0).payment()).isNull();
		}

		@DisplayName("벌레 충전 내역인 경우 결제 정보를 포함한다.")
		@WithMember
		@Test
		void charge_success() throws Exception {
			// given
			Long memberId = getAuthMember().id();
			Product product = productRepository.save(bugProduct());
			Payment payment = paymentRepository.save(payment(product));
			bugHistoryRepository.save(chargeGoldenBugHistory(memberId, payment));

			// expected
			String content = mockMvc.perform(get("/bugs/history")
					.contentType(APPLICATION_JSON))
				.andExpect(status().isOk())
				.andDo(print())
				.andReturn()
				.getResponse()
				.getContentAsString(UTF_8);
			BugHistoryResponse actual = objectMapper.readValue(content, BugHistoryResponse.class);
			assertThat(actual.history().get(0).bugType()).isEqualTo(BugType.GOLDEN);
			assertThat(actual.history().get(0).actionType()).isEqualTo(BugActionType.CHARGE);
			assertThat(actual.history().get(0).quantity()).isEqualTo(BUG_PRODUCT_QUANTITY);
			assertThat(actual.history().get(0).payment().orderName()).isEqualTo(BUG_PRODUCT_NAME);
			assertThat(actual.history().get(0).payment().totalAmount()).isEqualTo(BUG_PRODUCT_PRICE);
			assertThat(actual.history().get(0).payment().discountAmount()).isZero();
		}
	}

	@DisplayName("벌레 상품 목록을 조회한다.")
	@Test
	void get_bug_products_success() throws Exception {
		// given
		List<Product> products = productRepository.saveAll(List.of(bugProduct(), bugProduct()));
		ProductsResponse expected = ProductMapper.toProductsResponse(products);

		// expected
		String content = mockMvc.perform(get("/bugs/products")
				.contentType(APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(print())
			.andReturn()
			.getResponse()
			.getContentAsString(UTF_8);
		ProductsResponse actual = objectMapper.readValue(content, ProductsResponse.class);
		assertThat(actual).isEqualTo(expected);
	}

	@DisplayName("벌레 상품을 구매한다.")
	@WithMember
	@Test
	void purchase_bug_product_success() throws Exception {
		// given
		Product product = productRepository.save(bugProduct());
		PurchaseProductRequest request = new PurchaseProductRequest(null);

		// expected
		String content = mockMvc.perform(post("/bugs/products/{productId}/purchase", product.getId())
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andDo(print())
			.andReturn()
			.getResponse()
			.getContentAsString(UTF_8);
		PurchaseProductResponse actual = objectMapper.readValue(content, PurchaseProductResponse.class);
		assertThat(actual.orderName()).isEqualTo(BUG_PRODUCT_NAME);
		assertThat(actual.price()).isEqualTo(BUG_PRODUCT_PRICE);
	}
}
