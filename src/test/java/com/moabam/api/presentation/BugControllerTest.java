package com.moabam.api.presentation;

import static java.nio.charset.StandardCharsets.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moabam.api.application.BugService;
import com.moabam.api.dto.BugResponse;
import com.moabam.fixture.BugFixture;

@SpringBootTest
@AutoConfigureMockMvc
class BugControllerTest {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	ObjectMapper objectMapper;

	@MockBean
	BugService bugService;

	@DisplayName("벌레를 조회한다.")
	@Test
	void get_bug_success() throws Exception {
		// given
		Long memberId = 1L;
		BugResponse expected = BugFixture.bugResponse();
		given(bugService.getBug(memberId)).willReturn(expected);

		// when & then
		String content = mockMvc.perform(get("/bugs"))
			.andDo(print())
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString(UTF_8);
		BugResponse actual = objectMapper.readValue(content, BugResponse.class);
		Assertions.assertThat(actual).isEqualTo(expected);
	}
}
