package com.moabam.api.presentation;

import static com.moabam.global.common.util.GlobalConstant.*;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.moabam.api.domain.entity.Member;
import com.moabam.api.domain.entity.Room;
import com.moabam.api.domain.repository.MemberRepository;
import com.moabam.api.domain.repository.NotificationRepository;
import com.moabam.api.domain.repository.RoomRepository;
import com.moabam.global.common.repository.StringRedisRepository;
import com.moabam.support.fixture.MemberFixture;
import com.moabam.support.fixture.RoomFixture;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
class NotificationControllerTest {

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
		notificationRepository.deleteFcmTokenByMemberId(target.getId());
		stringRedisRepository.delete(knockKey);
	}

	@DisplayName("GET - 성공적으로 상대를 찔렀을 때, - Void")
	@Test
	void notificationController_sendKnockNotification() throws Exception {
		// Given
		notificationRepository.saveFcmToken(target.getId(), "FCM_TOKEN");

		// When & Then
		mockMvc.perform(get("/notifications/rooms/" + room.getId() + "/members/" + target.getId()))
			.andDo(print())
			.andDo(document("notifications",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint())))
			.andExpect(status().isOk());
	}

	@DisplayName("GET - 찌른 상대가 접속 중이 아닐 때, - NotFoundException")
	@Test
	void notificationController_sendKnockNotification_NotFoundException() throws Exception {
		// When & Then
		mockMvc.perform(get("/notifications/rooms/" + room.getId() + "/members/" + target.getId()))
			.andDo(print())
			.andDo(document("notifications",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint())))
			.andExpect(status().isNotFound());
	}

	@DisplayName("GET - 이미 찌른 대상일 때, - ConflictException")
	@Test
	void notificationController_sendKnockNotification_ConflictException() throws Exception {
		// Given
		notificationRepository.saveFcmToken(target.getId(), "FCM_TOKEN");
		notificationRepository.saveKnockNotification(knockKey);

		// When & Then
		mockMvc.perform(get("/notifications/rooms/" + room.getId() + "/members/" + target.getId()))
			.andDo(print())
			.andDo(document("notifications",
				preprocessRequest(prettyPrint()),
				preprocessResponse(prettyPrint())))
			.andExpect(status().isConflict());
	}
}
