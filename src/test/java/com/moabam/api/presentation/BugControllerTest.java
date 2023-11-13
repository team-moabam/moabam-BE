package com.moabam.api.presentation;

import static com.moabam.support.fixture.BugFixture.*;
import static java.nio.charset.StandardCharsets.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.http.MediaType.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moabam.api.application.BugService;
import com.moabam.api.dto.BugMapper;
import com.moabam.api.dto.BugResponse;
import com.moabam.support.common.WithoutFilterSupporter;

@WebMvcTest(controllers = BugController.class)
class BugControllerTest extends WithoutFilterSupporter {

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
		BugResponse expected = BugMapper.toBugResponse(bug());
		given(bugService.getBug(memberId)).willReturn(expected);

		// when, then
		String content = mockMvc.perform(get("/bugs")
				.contentType(APPLICATION_JSON))
			.andDo(print())
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString(UTF_8);
		BugResponse actual = objectMapper.readValue(content, BugResponse.class);
		assertThat(actual).isEqualTo(expected);
	}
}
