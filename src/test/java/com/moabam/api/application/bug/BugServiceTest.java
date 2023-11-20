package com.moabam.api.application.bug;

import static com.moabam.api.domain.product.ProductType.*;
import static com.moabam.support.fixture.MemberFixture.*;
import static com.moabam.support.fixture.ProductFixture.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.moabam.api.application.member.MemberService;
import com.moabam.api.domain.bug.Bug;
import com.moabam.api.domain.member.Member;
import com.moabam.api.domain.product.Product;
import com.moabam.api.domain.product.repository.ProductRepository;
import com.moabam.api.dto.bug.BugResponse;
import com.moabam.api.dto.product.ProductResponse;
import com.moabam.api.dto.product.ProductsResponse;
import com.moabam.api.dto.product.PurchaseProductRequest;
import com.moabam.global.common.util.StreamUtils;
import com.moabam.global.error.exception.NotFoundException;

@ExtendWith(MockitoExtension.class)
class BugServiceTest {

	@InjectMocks
	BugService bugService;

	@Mock
	MemberService memberService;

	@Mock
	ProductRepository productRepository;

	@DisplayName("벌레를 조회한다.")
	@Test
	void get_bug_success() {
		// given
		Long memberId = 1L;
		Member member = member();
		given(memberService.getById(memberId)).willReturn(member);

		// when
		BugResponse response = bugService.getBug(memberId);

		// then
		Bug bug = member.getBug();
		assertThat(response.morningBug()).isEqualTo(bug.getMorningBug());
		assertThat(response.nightBug()).isEqualTo(bug.getNightBug());
		assertThat(response.goldenBug()).isEqualTo(bug.getGoldenBug());
	}

	@DisplayName("벌레 상품 목록을 조회한다.")
	@Test
	void get_bug_products_success() {
		// given
		Product product1 = bugProduct();
		Product product2 = bugProduct();
		given(productRepository.findAllByType(BUG)).willReturn(List.of(product1, product2));

		// when
		ProductsResponse response = bugService.getBugProducts();

		// then
		List<String> productNames = StreamUtils.map(response.products(), ProductResponse::name);
		assertThat(response.products()).hasSize(2);
		assertThat(productNames).containsExactly(BUG_PRODUCT_NAME, BUG_PRODUCT_NAME);
	}

	@DisplayName("벌레 상품을 구매한다.")
	@Nested
	class PurchaseBugProduct {

		@DisplayName("해당 상품이 존재하지 않으면 예외가 발생한다.")
		@Test
		void product_not_found_exception() {
			// given
			Long memberId = 1L;
			Long productId = 1L;
			PurchaseProductRequest request = new PurchaseProductRequest(null);
			given(productRepository.findById(productId)).willReturn(Optional.empty());

			// when, then
			assertThatThrownBy(() -> bugService.purchaseBugProduct(memberId, productId, request))
				.isInstanceOf(NotFoundException.class)
				.hasMessage("존재하지 않는 상품입니다.");
		}
	}
}
