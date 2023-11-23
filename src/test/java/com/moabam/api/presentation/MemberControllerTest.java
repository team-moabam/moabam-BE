package com.moabam.api.presentation;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureMockRestServiceServer;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moabam.api.application.auth.OAuth2AuthorizationServerRequestService;
import com.moabam.api.domain.auth.repository.TokenRepository;
import com.moabam.api.domain.member.Member;
import com.moabam.api.domain.member.repository.MemberRepository;
import com.moabam.api.domain.member.repository.MemberSearchRepository;
import com.moabam.api.domain.room.repository.ParticipantRepository;
import com.moabam.api.domain.room.repository.RoomRepository;
import com.moabam.api.dto.auth.TokenSaveValue;
import com.moabam.global.config.OAuthConfig;
import com.moabam.global.error.exception.UnauthorizedException;
import com.moabam.global.error.handler.RestTemplateResponseHandler;
import com.moabam.support.annotation.WithMember;
import com.moabam.support.common.WithoutFilterSupporter;
import com.moabam.support.fixture.MemberFixture;
import com.moabam.support.fixture.TokenSaveValueFixture;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureMockRestServiceServer
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MemberControllerTest extends WithoutFilterSupporter {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	MemberRepository memberRepository;

	@Autowired
	MemberSearchRepository memberSearchRepository;

	@Autowired
	TokenRepository tokenRepository;

	@Autowired
	RoomRepository roomRepository;

	@Autowired
	ParticipantRepository participantRepository;

	@Autowired
	OAuth2AuthorizationServerRequestService oAuth2AuthorizationServerRequestService;

	@Autowired
	OAuthConfig oAuthConfig;

	RestTemplateBuilder restTemplateBuilder;

	MockRestServiceServer mockRestServiceServer;

	Member member;

	@BeforeAll
	void allSetUp() {
		restTemplateBuilder = new RestTemplateBuilder()
			.errorHandler(new RestTemplateResponseHandler());

		member = MemberFixture.member();
		memberRepository.save(member);
	}

	@BeforeEach
	void setUp() {
		RestTemplate restTemplate = restTemplateBuilder.build();
		ReflectionTestUtils.setField(oAuth2AuthorizationServerRequestService, "restTemplate", restTemplate);
		mockRestServiceServer = MockRestServiceServer.createServer(restTemplate);
	}

	@DisplayName("로그아웃 성공 테스트")
	@WithMember
	@Test
	void logout_success() throws Exception {
		// given
		TokenSaveValue tokenSaveValue = TokenSaveValueFixture.tokenSaveValue();
		tokenRepository.saveToken(member.getId(), tokenSaveValue);

		// expected
		ResultActions result = mockMvc.perform(get("/members/logout"));

		result.andExpect(status().is2xxSuccessful());

		Assertions.assertThatThrownBy(() -> tokenRepository.getTokenSaveValue(member.getId()))
			.isInstanceOf(UnauthorizedException.class);

	}

	@DisplayName("회원 삭제 성공 테스트")
	@WithMember
	@Test
	void delete_member_success() throws Exception {
		// Given
		String nickname = member.getNickname();

		// expected
		mockRestServiceServer.expect(requestTo(oAuthConfig.provider().unlink()))
			.andExpect(MockRestRequestMatchers.content()
				.contentType("application/x-www-form-urlencoded;charset=UTF-8"))
			.andExpect(MockRestRequestMatchers.header(
				"Authorization", "KakaoAK " + oAuthConfig.client().adminKey()))
			.andExpect(method(HttpMethod.POST))
			.andRespond(withStatus(HttpStatus.OK));

		mockMvc.perform(delete("/members"));
		memberRepository.flush();

		Optional<Member> deletedMemberOptional = memberRepository.findById(member.getId());
		assertThat(deletedMemberOptional).isNotEmpty();

		Member deletedMEmber = deletedMemberOptional.get();
		assertThat(deletedMEmber.getDeletedAt()).isNotNull();
		assertThat(deletedMEmber.getNickname()).isEqualTo(nickname);
	}

	@DisplayName("회원이 없어서 회원 삭제 실패")
	@WithMember(id = 123L)
	@Test
	void delete_member_failBy_not_found_member() throws Exception {
		// expected
		mockMvc.perform(delete("/members"))
			.andExpect(status().isNotFound());
	}

	@DisplayName("연결 오류로 인한 카카오 연결 끊기 실패로 롤백")
	@WithMember
	@ParameterizedTest
	@ValueSource(ints = {401, 400})
	void unlink_social_member_failby_connection_error_and_rollback(int code) throws Exception {
		// expected
		mockRestServiceServer.expect(requestTo(oAuthConfig.provider().unlink()))
			.andExpect(MockRestRequestMatchers.header(
				"Content-Type", "application/x-www-form-urlencoded;charset=UTF-8"))
			.andExpect(MockRestRequestMatchers.header(
				"Authorization", "KakaoAK " + oAuthConfig.client().adminKey()))
			.andExpect(method(HttpMethod.POST))
			.andRespond(withStatus(HttpStatusCode.valueOf(code)));

		ResultActions result = mockMvc.perform(delete("/members"));
		result.andExpect(status().isBadRequest());

		Optional<Member> rollbackMemberOptional = memberSearchRepository.findMember(member.getId());
		assertThat(rollbackMemberOptional).isPresent();

		Member rollMember = rollbackMemberOptional.get();
		assertAll(
			() -> assertThat(rollMember.getSocialId()).isEqualTo(member.getSocialId()),
			() -> assertThat(rollMember.getDeletedAt()).isNull()
		);
	}
}
