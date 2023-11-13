package com.moabam.api.presentation;

import static com.moabam.support.fixture.ItemFixture.*;
import static java.nio.charset.StandardCharsets.*;
import static java.util.Collections.*;
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
import com.moabam.api.application.ItemService;
import com.moabam.api.domain.entity.Item;
import com.moabam.api.domain.entity.enums.BugType;
import com.moabam.api.domain.entity.enums.ItemType;
import com.moabam.api.dto.ItemMapper;
import com.moabam.api.dto.ItemsResponse;
import com.moabam.api.dto.PurchaseItemRequest;
import com.moabam.support.annotation.WithMember;
import com.moabam.support.common.WithoutFilterSupporter;

@WebMvcTest(controllers = ItemController.class)
class ItemControllerTest extends WithoutFilterSupporter {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	ObjectMapper objectMapper;

	@MockBean
	ItemService itemService;

	@DisplayName("아이템 목록을 조회한다.")
	@WithMember
	@Test
	void get_items_success() throws Exception {
		// given
		Long memberId = 1L;
		ItemType type = ItemType.MORNING;
		Item item1 = morningSantaSkin().build();
		Item item2 = morningKillerSkin().build();
		ItemsResponse expected = ItemMapper.toItemsResponse(List.of(item1, item2), emptyList());
		given(itemService.getItems(memberId, type)).willReturn(expected);

		// when, then
		String content = mockMvc.perform(
				get("/items").param("type", ItemType.MORNING.name()).contentType(APPLICATION_JSON))
			.andDo(print())
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString(UTF_8);
		ItemsResponse actual = objectMapper.readValue(content, ItemsResponse.class);
		assertThat(actual).isEqualTo(expected);
	}

	@DisplayName("아이템을 구매한다.")
	@Test
	void purchase_item_success() throws Exception {
		// given
		Long memberId = 1L;
		Long itemId = 1L;
		PurchaseItemRequest request = new PurchaseItemRequest(BugType.MORNING);

		// when, then
		mockMvc.perform(post("/items/{itemId}/purchase", itemId).contentType(APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(request))).andDo(print()).andExpect(status().isOk());
		verify(itemService).purchaseItem(memberId, itemId, request);
	}

	@DisplayName("아이템을 적용한다.")
	@WithMember
	@Test
	void select_item_success() throws Exception {
		// given
		Long memberId = 1L;
		Long itemId = 1L;

		// when, then
		mockMvc.perform(post("/items/{itemId}/select", itemId).contentType(APPLICATION_JSON))
			.andDo(print())
			.andExpect(status().isOk());
		verify(itemService).selectItem(memberId, itemId);
	}
}
