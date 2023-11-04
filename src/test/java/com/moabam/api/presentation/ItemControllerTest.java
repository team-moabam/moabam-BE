package com.moabam.api.presentation;

import static java.nio.charset.StandardCharsets.*;
import static java.util.Collections.*;
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
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moabam.api.application.ItemService;
import com.moabam.api.domain.entity.Item;
import com.moabam.api.domain.entity.enums.RoomType;
import com.moabam.api.dto.ItemMapper;
import com.moabam.api.dto.ItemsResponse;
import com.moabam.fixture.ItemFixture;

@SpringBootTest
@AutoConfigureMockMvc
class ItemControllerTest {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	ObjectMapper objectMapper;

	@MockBean
	ItemService itemService;

	@DisplayName("아이템 목록을 조회한다.")
	@Test
	void get_items_success() throws Exception {
		// given
		Long memberId = 1L;
		RoomType type = RoomType.MORNING;
		Item item1 = ItemFixture.morningSantaSkin().build();
		Item item2 = ItemFixture.morningKillerSkin().build();
		ItemsResponse expected = ItemMapper.toItemsResponse(List.of(item1, item2), emptyList());
		given(itemService.getItems(memberId, type)).willReturn(expected);

		// expected
		String content = mockMvc.perform(get("/items")
				.param("type", RoomType.MORNING.name()))
			.andDo(print())
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString(UTF_8);
		ItemsResponse actual = objectMapper.readValue(content, ItemsResponse.class);
		Assertions.assertThat(actual).isEqualTo(expected);
	}
}
