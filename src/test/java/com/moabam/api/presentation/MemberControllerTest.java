package com.moabam.api.presentation;

import static com.moabam.global.common.util.GlobalConstant.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureMockRestServiceServer;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moabam.api.application.auth.OAuth2AuthorizationServerRequestService;
import com.moabam.api.application.image.ImageService;
import com.moabam.api.application.member.MemberMapper;
import com.moabam.api.domain.auth.repository.TokenRepository;
import com.moabam.api.domain.image.ImageType;
import com.moabam.api.domain.item.Inventory;
import com.moabam.api.domain.item.Item;
import com.moabam.api.domain.item.repository.InventoryRepository;
import com.moabam.api.domain.item.repository.ItemRepository;
import com.moabam.api.domain.member.Badge;
import com.moabam.api.domain.member.BadgeType;
import com.moabam.api.domain.member.Member;
import com.moabam.api.domain.member.Role;
import com.moabam.api.domain.member.repository.BadgeRepository;
import com.moabam.api.domain.member.repository.MemberRepository;
import com.moabam.api.domain.member.repository.MemberSearchRepository;
import com.moabam.api.domain.room.Participant;
import com.moabam.api.domain.room.Room;
import com.moabam.api.domain.room.repository.ParticipantRepository;
import com.moabam.api.domain.room.repository.RoomRepository;
import com.moabam.api.dto.auth.TokenSaveValue;
import com.moabam.api.dto.member.ModifyMemberRequest;
import com.moabam.api.dto.ranking.RankingInfo;
import com.moabam.global.config.EmbeddedRedisConfig;
import com.moabam.global.config.OAuthConfig;
import com.moabam.global.error.exception.UnauthorizedException;
import com.moabam.global.error.handler.RestTemplateResponseHandler;
import com.moabam.support.annotation.WithMember;
import com.moabam.support.common.WithoutFilterSupporter;
import com.moabam.support.fixture.BadgeFixture;
import com.moabam.support.fixture.InventoryFixture;
import com.moabam.support.fixture.ItemFixture;
import com.moabam.support.fixture.MemberFixture;
import com.moabam.support.fixture.ParticipantFixture;
import com.moabam.support.fixture.RoomFixture;
import com.moabam.support.fixture.TokenSaveValueFixture;

import jakarta.persistence.EntityManager;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureMockRestServiceServer
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Import(EmbeddedRedisConfig.class)
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
	ItemRepository itemRepository;

	@Autowired
	BadgeRepository badgeRepository;

	@Autowired
	InventoryRepository inventoryRepository;

	@Autowired
	ParticipantRepository participantRepository;

	@Autowired
	OAuth2AuthorizationServerRequestService oAuth2AuthorizationServerRequestService;

	@Autowired
	OAuthConfig oAuthConfig;

	@SpyBean
	ImageService imageService;

	RestTemplateBuilder restTemplateBuilder;

	MockRestServiceServer mockRestServiceServer;

	Member member;

	@Autowired
	EntityManager entityManager;

	@Autowired
	RedisTemplate<String, Object> redisTemplate;

	@BeforeAll
	void allSetUp() {
		restTemplateBuilder = new RestTemplateBuilder()
			.errorHandler(new RestTemplateResponseHandler());

		member = MemberFixture.member("1234567890987654");
		member.increaseTotalCertifyCount();
		memberRepository.save(member);
	}

	@BeforeEach
	void setUp() {
		RestTemplate restTemplate = restTemplateBuilder.build();
		ReflectionTestUtils.setField(oAuth2AuthorizationServerRequestService, "restTemplate", restTemplate);
		mockRestServiceServer = MockRestServiceServer.createServer(restTemplate);
		member = entityManager.merge(member);
	}

	@DisplayName("로그아웃 성공 테스트")
	@WithMember
	@Test
	void logout_success() throws Exception {
		// given
		TokenSaveValue tokenSaveValue = TokenSaveValueFixture.tokenSaveValue();
		tokenRepository.saveToken(member.getId(), tokenSaveValue, Role.USER);

		// expected
		ResultActions result = mockMvc.perform(get("/members/logout"));

		result.andExpect(status().is2xxSuccessful());

		Assertions.assertThatThrownBy(() -> tokenRepository.getTokenSaveValue(member.getId(), Role.USER))
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
		assertThat(deletedMEmber.getNickname()).isNull();
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

	@DisplayName("방장으로 인해 회원 삭제 조회 실패")
	@WithMember
	@Test
	void unlink_social_member_failby_meber_is_manger() throws Exception {
		// given
		Room room = RoomFixture.room();
		room.changeManagerNickname(member.getNickname());

		Participant participant = ParticipantFixture.participant(room, member.getId());
		participant.enableManager();
		roomRepository.save(room);
		participantRepository.save(participant);

		// then
		mockMvc.perform(delete("/members"))
			.andExpect(status().isNotFound());
	}

	@DisplayName("내 정보 조회 성공")
	@WithMember
	@Test
	void search_my_info_success() throws Exception {
		// given
		Badge birth = BadgeFixture.badge(member.getId(), BadgeType.BIRTH);
		Badge level50 = BadgeFixture.badge(member.getId(), BadgeType.LEVEL50);
		Badge level10 = BadgeFixture.badge(member.getId(), BadgeType.LEVEL10);
		List<Badge> badges = List.of(birth, level10, level50);
		badgeRepository.saveAll(badges);

		Item night = ItemFixture.nightMageSkin();
		Item morning = ItemFixture.morningSantaSkin().build();
		Item killer = ItemFixture.morningKillerSkin().build();
		itemRepository.saveAll(List.of(night, morning, killer));

		Inventory nightInven = InventoryFixture.inventory(member.getId(), night);
		nightInven.select(member);

		Inventory morningInven = InventoryFixture.inventory(member.getId(), morning);
		morningInven.select(member);

		Inventory killerInven = InventoryFixture.inventory(member.getId(), killer);
		inventoryRepository.saveAll(List.of(nightInven, morningInven, killerInven));

		member.changeDefaultSkintUrl(night);
		member.changeDefaultSkintUrl(morning);
		memberRepository.flush();

		// expected
		mockMvc.perform(get("/members"))
			.andExpect(status().isOk())
			.andExpectAll(
				MockMvcResultMatchers.jsonPath("$.nickname").value(member.getNickname()),
				MockMvcResultMatchers.jsonPath("$.profileImage").value(member.getProfileImage()),
				MockMvcResultMatchers.jsonPath("$.intro").value(member.getIntro()),
				MockMvcResultMatchers.jsonPath("$.level").value(member.getTotalCertifyCount() / LEVEL_DIVISOR),
				MockMvcResultMatchers.jsonPath("$.exp").value(member.getTotalCertifyCount() % LEVEL_DIVISOR),

				// MockMvcResultMatchers.jsonPath("$.birds.MORNING").value(morningInven.getItem().getImage()),
				// MockMvcResultMatchers.jsonPath("$.birds.NIGHT").value(nightInven.getItem().getImage()),

				MockMvcResultMatchers.jsonPath("$.badges[0].badge").value("탄생 축하 뱃지"),
				MockMvcResultMatchers.jsonPath("$.badges[0].unlock").value(true),
				MockMvcResultMatchers.jsonPath("$.badges[1].badge").value("10레벨 뱃지"),
				MockMvcResultMatchers.jsonPath("$.badges[1].unlock").value(true),
				MockMvcResultMatchers.jsonPath("$.badges[2].badge").value("50레벨 뱃지"),
				MockMvcResultMatchers.jsonPath("$.badges[2].unlock").value(true),
				MockMvcResultMatchers.jsonPath("$.goldenBug").value(member.getBug().getGoldenBug()),
				MockMvcResultMatchers.jsonPath("$.morningBug").value(member.getBug().getMorningBug()),
				MockMvcResultMatchers.jsonPath("$.nightBug").value(member.getBug().getNightBug())
			).andDo(print());
	}

	@DisplayName("뱃지없는 내 정보 조회 성공")
	@WithMember
	@Test
	void search_my_info_with_no_badge_success() throws Exception {
		// given
		Item night = ItemFixture.nightMageSkin();
		Item morning = ItemFixture.morningSantaSkin().build();
		Item killer = ItemFixture.morningKillerSkin().build();
		itemRepository.saveAll(List.of(night, morning, killer));

		Inventory nightInven = InventoryFixture.inventory(member.getId(), night);
		nightInven.select(member);

		Inventory morningInven = InventoryFixture.inventory(member.getId(), morning);
		morningInven.select(member);

		Inventory killerInven = InventoryFixture.inventory(member.getId(), killer);
		inventoryRepository.saveAll(List.of(nightInven, morningInven, killerInven));

		member.changeDefaultSkintUrl(night);
		member.changeDefaultSkintUrl(morning);

		memberRepository.flush();

		// expected
		mockMvc.perform(get("/members"))
			.andExpect(status().isOk())
			.andExpectAll(
				MockMvcResultMatchers.jsonPath("$.nickname").value(member.getNickname()),
				MockMvcResultMatchers.jsonPath("$.profileImage").value(member.getProfileImage()),
				MockMvcResultMatchers.jsonPath("$.intro").value(member.getIntro()),
				MockMvcResultMatchers.jsonPath("$.level").value(member.getTotalCertifyCount() / LEVEL_DIVISOR),
				MockMvcResultMatchers.jsonPath("$.exp").value(member.getTotalCertifyCount() % LEVEL_DIVISOR),

				// MockMvcResultMatchers.jsonPath("$.birds.MORNING").value(morningInven.getItem().getImage()),
				// MockMvcResultMatchers.jsonPath("$.birds.NIGHT").value(nightInven.getItem().getImage()),

				MockMvcResultMatchers.jsonPath("$.badges[0].badge").value("탄생 축하 뱃지"),
				MockMvcResultMatchers.jsonPath("$.badges[0].unlock").value(false),
				MockMvcResultMatchers.jsonPath("$.badges[1].badge").value("10레벨 뱃지"),
				MockMvcResultMatchers.jsonPath("$.badges[1].unlock").value(false),
				MockMvcResultMatchers.jsonPath("$.badges[2].badge").value("50레벨 뱃지"),
				MockMvcResultMatchers.jsonPath("$.badges[2].unlock").value(false),
				MockMvcResultMatchers.jsonPath("$.goldenBug").value(member.getBug().getGoldenBug()),
				MockMvcResultMatchers.jsonPath("$.morningBug").value(member.getBug().getMorningBug()),
				MockMvcResultMatchers.jsonPath("$.nightBug").value(member.getBug().getNightBug())
			).andDo(print());
	}

	@DisplayName("친구 정보 조회 성공")
	@WithMember
	@Test
	void search_friend_info_success() throws Exception {
		// given
		Member friend = MemberFixture.member("123456789");
		memberRepository.save(friend);

		Badge birth = BadgeFixture.badge(friend.getId(), BadgeType.BIRTH);
		Badge level10 = BadgeFixture.badge(friend.getId(), BadgeType.LEVEL10);
		List<Badge> badges = List.of(birth, level10);
		badgeRepository.saveAll(badges);

		Item night = ItemFixture.nightMageSkin();
		Item morning = ItemFixture.morningSantaSkin().build();
		Item killer = ItemFixture.morningKillerSkin().build();
		itemRepository.saveAll(List.of(night, morning, killer));

		Inventory nightInven = InventoryFixture.inventory(friend.getId(), night);
		nightInven.select(friend);

		Inventory morningInven = InventoryFixture.inventory(friend.getId(), morning);
		morningInven.select(friend);

		Inventory killerInven = InventoryFixture.inventory(friend.getId(), killer);
		friend.changeDefaultSkintUrl(morning);
		friend.changeDefaultSkintUrl(night);
		memberRepository.flush();
		inventoryRepository.saveAll(List.of(nightInven, morningInven, killerInven));

		friend.changeDefaultSkintUrl(morning);
		friend.changeDefaultSkintUrl(night);
		memberRepository.flush();

		// expected
		mockMvc.perform(get("/members/{memberId}", friend.getId()))
			.andExpect(status().isOk())
			.andExpectAll(
				MockMvcResultMatchers.jsonPath("$.nickname").value(friend.getNickname()),
				MockMvcResultMatchers.jsonPath("$.profileImage").value(friend.getProfileImage()),
				MockMvcResultMatchers.jsonPath("$.intro").value(friend.getIntro()),
				MockMvcResultMatchers.jsonPath("$.level").value(friend.getTotalCertifyCount() / LEVEL_DIVISOR),
				MockMvcResultMatchers.jsonPath("$.exp").value(friend.getTotalCertifyCount() % LEVEL_DIVISOR),

				MockMvcResultMatchers.jsonPath("$.birds.MORNING").value(morningInven.getItem().getAwakeImage()),
				MockMvcResultMatchers.jsonPath("$.birds.NIGHT").value(nightInven.getItem().getAwakeImage()),

				MockMvcResultMatchers.jsonPath("$.badges[0].badge").value("탄생 축하 뱃지"),
				MockMvcResultMatchers.jsonPath("$.badges[0].unlock").value(true),
				MockMvcResultMatchers.jsonPath("$.badges[1].badge").value("10레벨 뱃지"),
				MockMvcResultMatchers.jsonPath("$.badges[1].unlock").value(true),
				MockMvcResultMatchers.jsonPath("$.badges[2].badge").value("50레벨 뱃지"),
				MockMvcResultMatchers.jsonPath("$.badges[2].unlock").value(false)
			).andDo(print());
	}

	@DisplayName("회원 정보 찾기 실패로 예외 발생")
	@WithMember(id = 123L)
	@Test
	void search_member_failBy_not_found_member() throws Exception {
		// expected
		mockMvc.perform(get("/members/{memberId}", 123L))
			.andExpect(status().is4xxClientError());
	}

	@DisplayName("기본 스킨의 갯수가 다를때 예외 발생")
	@Test
	void search_member_failBy_default_skin_size() throws Exception {
		// given
		Item night = ItemFixture.nightMageSkin();
		Item morning = ItemFixture.morningSantaSkin().build();
		Item killer = ItemFixture.morningKillerSkin().build();
		itemRepository.saveAll(List.of(night, morning, killer));

		Inventory nightInven = InventoryFixture.inventory(member.getId(), night);
		nightInven.select(member);

		Inventory morningInven = InventoryFixture.inventory(member.getId(), morning);
		morningInven.select(member);

		Inventory killerInven = InventoryFixture.inventory(member.getId(), killer);
		killerInven.select(member);
		inventoryRepository.saveAll(List.of(nightInven, morningInven, killerInven));

		// expected
		mockMvc.perform(get("/members/{memberId}", 123L))
			.andExpect(status().is4xxClientError());
	}

	@DisplayName("회원 정보 요청 성공")
	@WithMember
	@ParameterizedTest
	@CsvSource({"intro,", ", nickname", ",", "intro, nickname"})
	void member_modify_request_success(String intro, String nickname) throws Exception {
		// given
		ModifyMemberRequest request = new ModifyMemberRequest(intro, nickname);
		MockMultipartFile newProfileImage =
			new MockMultipartFile(
				"profileImage",
				"tooth.png",
				"multipart/form-data",
				"uploadFile".getBytes(StandardCharsets.UTF_8));
		MockMultipartFile modifyMemberRequest =
			new MockMultipartFile(
				"modifyMemberRequest",
				null,
				"application/json",
				objectMapper.writeValueAsString(request).getBytes(StandardCharsets.UTF_8));

		willReturn(List.of("/main"))
			.given(imageService).uploadImages(List.of(newProfileImage), ImageType.PROFILE_IMAGE);

		// expected
		mockMvc.perform(multipart(HttpMethod.POST, "/members/modify")
				.file(modifyMemberRequest)
				.file(newProfileImage)
				.contentType("multipart/form-data")

				.characterEncoding("UTF-8"))
			.andExpect(status().is2xxSuccessful())
			.andDo(print());

	}

	@DisplayName("회원 프로필없이 성공 ")
	@WithMember
	@ParameterizedTest
	@CsvSource({"intro,", ", nickname", ",", "intro, nickname"})
	void member_modify_no_image_request_success(String intro, String nickname) throws Exception {
		// given
		ModifyMemberRequest request = new ModifyMemberRequest(intro, nickname);
		MockMultipartFile modifyMemberRequest =
			new MockMultipartFile(
				"modifyMemberRequest",
				null,
				"application/json",
				objectMapper.writeValueAsString(request).getBytes(StandardCharsets.UTF_8));

		willThrow(NullPointerException.class)
			.given(imageService).uploadImages(any(), any());
		RankingInfo rankingInfo = MemberMapper.toRankingInfo(member);
		redisTemplate.opsForZSet().add("Ranking", rankingInfo, member.getTotalCertifyCount());

		// expected
		mockMvc.perform(multipart(HttpMethod.POST, "/members/modify")
				.file(modifyMemberRequest)
				.contentType("multipart/form-data")

				.characterEncoding("UTF-8"))
			.andExpect(status().is2xxSuccessful())
			.andDo(print());

		String updateNick = member.getNickname();

		if (Objects.nonNull(nickname)) {
			updateNick = nickname;
		}

		Double result = redisTemplate.opsForZSet()
			.score("Ranking", new RankingInfo(member.getId(), updateNick, member.getProfileImage()));
		assertThat(result).isEqualTo(member.getTotalCertifyCount());
	}
}
