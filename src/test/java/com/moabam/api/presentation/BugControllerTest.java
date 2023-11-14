package com.moabam.api.presentation;

import static com.moabam.support.fixture.MemberFixture.*;
import static org.springframework.http.MediaType.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moabam.api.domain.member.repository.MemberRepository;
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

	@Autowired
	MemberRepository memberRepository;

	@DisplayName("벌레를 조회한다.")
	@Nested
	class GetBug {

		@DisplayName("성공한다.")
		@WithMember
		@Test
		void success() throws Exception {
			// given
			memberRepository.save(member());

			// expected
			mockMvc.perform(get("/bugs")
					.contentType(APPLICATION_JSON))
				.andExpect(status().isOk())
				.andDo(print());
		}

		@DisplayName("해당 회원이 존재하지 않으면 예외가 발생한다.")
		@WithMember
		@Test
		void member_not_found_exception() throws Exception {
			mockMvc.perform(get("/bugs")
					.contentType(APPLICATION_JSON))
				.andExpect(status().isNotFound())
				.andDo(print());
		}
	}
}
