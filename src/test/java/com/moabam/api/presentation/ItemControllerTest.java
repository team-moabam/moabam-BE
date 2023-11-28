package com.moabam.api.presentation;

import static com.moabam.global.auth.model.AuthorizationThreadLocal.*;
import static com.moabam.support.fixture.InventoryFixture.*;
import static com.moabam.support.fixture.ItemFixture.*;
import static com.moabam.support.fixture.MemberFixture.*;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moabam.api.application.bug.BugService;
import com.moabam.api.application.item.ItemMapper;
import com.moabam.api.application.member.MemberService;
import com.moabam.api.domain.bug.BugType;
import com.moabam.api.domain.item.Inventory;
import com.moabam.api.domain.item.Item;
import com.moabam.api.domain.item.ItemType;
import com.moabam.api.domain.item.repository.InventoryRepository;
import com.moabam.api.domain.item.repository.ItemRepository;
import com.moabam.api.dto.item.ItemsResponse;
import com.moabam.api.dto.item.PurchaseItemRequest;
import com.moabam.support.annotation.WithMember;
import com.moabam.support.common.WithoutFilterSupporter;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class ItemControllerTest extends WithoutFilterSupporter {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	ObjectMapper objectMapper;

	@MockBean
	MemberService memberService;

	@MockBean
	BugService bugService;

	@Autowired
	ItemRepository itemRepository;

	@Autowired
	InventoryRepository inventoryRepository;

	@DisplayName("아이템 목록을 조회한다.")
	@Nested
	class GetItems {

		@DisplayName("성공한다.")
		@WithMember
		@Test
		void success() throws Exception {
			// given
			Long memberId = getAuthMember().id();
			Item item1 = itemRepository.save(morningSantaSkin().build());
			Inventory inventory = inventoryRepository.save(inventory(memberId, item1));
			inventory.select();
			Item item2 = itemRepository.save(morningKillerSkin().build());
			ItemsResponse expected = ItemMapper.toItemsResponse(item1.getId(), List.of(item1), List.of(item2));

			// expected
			String content = mockMvc.perform(get("/items")
					.param("type", ItemType.MORNING.name())
					.contentType(APPLICATION_JSON))
				.andExpect(status().isOk())
				.andDo(print())
				.andReturn()
				.getResponse()
				.getContentAsString(UTF_8);
			ItemsResponse actual = objectMapper.readValue(content, ItemsResponse.class);
			assertThat(actual).isEqualTo(expected);
		}

		@DisplayName("아이템 타입이 유효하지 않으면 예외가 발생한다.")
		@WithMember
		@ParameterizedTest
		@ValueSource(strings = {"HI", ""})
		void item_type_bad_request_exception(String itemType) throws Exception {
			mockMvc.perform(get("/items")
					.param("type", itemType)
					.contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andDo(print());
		}
	}

	@Nested
	@DisplayName("아이템을 구매한다.")
	class PurchaseItem {

		@DisplayName("성공한다.")
		@WithMember
		@Test
		void success() throws Exception {
			// given
			Long memberId = getAuthMember().id();
			Item item = itemRepository.save(nightMageSkin());
			PurchaseItemRequest request = new PurchaseItemRequest(BugType.NIGHT);
			given(memberService.findMember(memberId)).willReturn(member());

			// expected
			mockMvc.perform(post("/items/{itemId}/purchase", item.getId())
					.contentType(APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andDo(print());
		}

		@DisplayName("아이템 구매 요청 바디가 유효하지 않으면 예외가 발생한다.")
		@WithMember
		@Test
		void bad_request_body_exception() throws Exception {
			// given
			Long itemId = 1L;
			PurchaseItemRequest request = new PurchaseItemRequest(null);

			// expected
			mockMvc.perform(post("/items/{itemId}/purchase", itemId)
					.contentType(APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("올바른 요청 정보가 아닙니다."))
				.andDo(print());
		}
	}

	@DisplayName("아이템을 적용한다.")
	@WithMember
	@Test
	void select_item_success() throws Exception {
		// given
		Long memberId = getAuthMember().id();
		Item item = itemRepository.save(nightMageSkin());
		inventoryRepository.save(inventory(memberId, item));

		// when, then
		mockMvc.perform(post("/items/{itemId}/select", item.getId())
				.contentType(APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(print());
	}
}
