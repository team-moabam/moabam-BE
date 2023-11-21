package com.moabam.api.presentation;

import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.moabam.api.domain.member.Member;
import com.moabam.api.domain.member.repository.MemberRepository;
import com.moabam.api.domain.notification.repository.NotificationRepository;
import com.moabam.api.domain.room.Room;
import com.moabam.api.domain.room.repository.RoomRepository;
import com.moabam.api.infrastructure.fcm.FcmRepository;
import com.moabam.api.infrastructure.fcm.FcmService;
import com.moabam.api.infrastructure.redis.StringRedisRepository;
import com.moabam.global.error.model.ErrorMessage;
import com.moabam.support.annotation.WithMember;
import com.moabam.support.common.WithoutFilterSupporter;
import com.moabam.support.fixture.ErrorSnippetFixture;
import com.moabam.support.fixture.MemberFixture;
import com.moabam.support.fixture.RoomFixture;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
class NotificationControllerTest extends WithoutFilterSupporter {

	private static final String KNOCK_KEY = "room_%s_member_%s_knocks_%s";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private RoomRepository roomRepository;

	@Autowired
	private NotificationRepository notificationRepository;

	@Autowired
	private StringRedisRepository stringRedisRepository;

	@Autowired
	private FcmService fcmService;

	@Autowired
	private FcmRepository fcmRepository;

	@MockBean
	private FirebaseMessaging firebaseMessaging;

	private Member target;
	private Room room;
	private String knockKey;

	@BeforeEach
	void setUp() {
		target = memberRepository.save(MemberFixture.member(123L, "targetName"));
		room = roomRepository.save(RoomFixture.room());
		knockKey = String.format(KNOCK_KEY, room.getId(), 1, target.getId());

		willReturn(null)
			.given(firebaseMessaging)
			.sendAsync(any(Message.class));
	}

	@AfterEach
	void setDown() {
		fcmService.deleteTokenByMemberId(target.getId());
		stringRedisRepository.delete(knockKey);
	}

	@WithMember
	@DisplayName("POST - 성공적으로 FCM Token을 저장한다. - Void")
	@Test
	void createFcmToken() throws Exception {
		// When & Then
		mockMvc.perform(post("/notifications")
				.param("fcmToken", "FCM-TOKEN"))
			.andDo(print())
			.andDo(document("notifications",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint())))
			.andExpect(status().isOk());
	}

	@WithMember
	@DisplayName("GET - 성공적으로 상대에게 콕 알림을 보낸다. - Void")
	@Test
	void sendKnock() throws Exception {
		// Given
		fcmRepository.saveToken(target.getId(), "FCM_TOKEN");

		// When & Then
		mockMvc.perform(get("/notifications/rooms/" + room.getId() + "/members/" + target.getId()))
			.andDo(print())
			.andDo(document("notifications/rooms/roomId/members/memberId",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint())))
			.andExpect(status().isOk());
	}

	@WithMember
	@DisplayName("GET - 콕 알림을 보낸 상대가 접속 중이 아니다. - NotFoundException")
	@Test
	void sendKnock_NotFoundException() throws Exception {
		// When & Then
		mockMvc.perform(get("/notifications/rooms/" + room.getId() + "/members/" + target.getId()))
			.andDo(print())
			.andDo(document("notifications/rooms/roomId/members/memberId",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				ErrorSnippetFixture.ERROR_MESSAGE_RESPONSE))
			.andExpect(status().isNotFound())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.message").value(ErrorMessage.NOT_FOUND_FCM_TOKEN.getMessage()));
	}

	@WithMember
	@DisplayName("GET - 이미 콕 알림을 보낸 대상이다. - ConflictException")
	@Test
	void sendKnock_ConflictException() throws Exception {
		// Given
		fcmRepository.saveToken(target.getId(), "FCM_TOKEN");
		notificationRepository.saveKnock(knockKey);

		// When & Then
		mockMvc.perform(get("/notifications/rooms/" + room.getId() + "/members/" + target.getId()))
			.andDo(print())
			.andDo(document("notifications/rooms/roomId/members/memberId",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint()),
				ErrorSnippetFixture.ERROR_MESSAGE_RESPONSE))
			.andExpect(status().isConflict())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.message").value(ErrorMessage.CONFLICT_KNOCK.getMessage()));
	}
}
