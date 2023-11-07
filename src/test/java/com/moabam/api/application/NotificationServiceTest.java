package com.moabam.api.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.moabam.api.domain.entity.Participant;
import com.moabam.api.domain.entity.Room;
import com.moabam.api.domain.repository.NotificationRepository;
import com.moabam.api.domain.repository.ParticipantSearchRepository;
import com.moabam.fixture.ParticipantFixture;
import com.moabam.fixture.RoomFixture;
import com.moabam.global.common.annotation.MemberTest;
import com.moabam.global.error.exception.ConflictException;
import com.moabam.global.error.exception.NotFoundException;
import com.moabam.global.error.model.ErrorMessage;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

	@InjectMocks
	private NotificationService notificationService;

	@Mock
	private RoomService roomService;

	@Mock
	private FirebaseMessaging firebaseMessaging;

	@Mock
	private NotificationRepository notificationRepository;

	@Mock
	private ParticipantSearchRepository participantSearchRepository;

	private MemberTest memberTest;

	@BeforeEach
	void setUp() {
		memberTest = new MemberTest(2L, "nickname");
	}

	@DisplayName("성공적으로 상대를 콕 찔렀을 때, - Void")
	@Test
	void notificationService_sendKnockNotification() {
		// Given
		willDoNothing().given(roomService).validateRoomById(any(Long.class));
		given(notificationRepository.existsFcmTokenByMemberId(any(Long.class))).willReturn(true);
		given(notificationRepository.existsByKey(any(String.class))).willReturn(false);
		given(notificationRepository.findFcmTokenByMemberId(any(Long.class))).willReturn("FCM-TOKEN");

		// When
		notificationService.sendKnockNotification(memberTest, 2L, 1L);

		// Then
		verify(firebaseMessaging).sendAsync(any(Message.class));
		verify(notificationRepository).saveKnockNotification(any(String.class));
	}

	@DisplayName("콕 찌를 상대의 방이 존재하지 않을 때, - NotFoundException")
	@Test
	void notificationService_sendKnockNotification_Room_NotFoundException() {
		// Given
		willThrow(NotFoundException.class).given(roomService).validateRoomById(any(Long.class));

		// When & Then
		assertThatThrownBy(() -> notificationService.sendKnockNotification(memberTest, 1L, 1L))
			.isInstanceOf(NotFoundException.class);
	}

	@DisplayName("콕 찌를 상대의 FCM 토큰이 존재하지 않을 때, - NotFoundException")
	@Test
	void notificationService_sendKnockNotification_FcmToken_NotFoundException() {
		// Given
		willDoNothing().given(roomService).validateRoomById(any(Long.class));
		given(notificationRepository.existsByKey(any(String.class))).willReturn(false);
		given(notificationRepository.existsFcmTokenByMemberId(any(Long.class))).willReturn(false);

		// When & Then
		assertThatThrownBy(() -> notificationService.sendKnockNotification(memberTest, 1L, 1L))
			.isInstanceOf(NotFoundException.class)
			.hasMessage(ErrorMessage.NOT_FOUND_FCM_TOKEN.getMessage());
	}

	@DisplayName("콕 찌를 상대가 이미 찌른 상대일 때, - ConflictException")
	@Test
	void notificationService_sendKnockNotification_ConflictException() {
		// Given
		willDoNothing().given(roomService).validateRoomById(any(Long.class));
		given(notificationRepository.existsByKey(any(String.class))).willReturn(true);

		// When & Then
		assertThatThrownBy(() -> notificationService.sendKnockNotification(memberTest, 1L, 1L))
			.isInstanceOf(ConflictException.class)
			.hasMessage(ErrorMessage.CONFLICT_KNOCK.getMessage());
	}

	@DisplayName("특정 인증 시간에 해당하는 방 사용자들에게 알림을 성공적으로 보낼 때, - Void")
	@MethodSource("provideParticipants")
	@ParameterizedTest
	void notificationService_sendCertificationTimeNotification(List<Participant> participants) {
		// Given
		given(participantSearchRepository.findAllByRoomCertifyTime(any(Integer.class))).willReturn(participants);
		given(notificationRepository.findFcmTokenByMemberId(any(Long.class))).willReturn("FCM-TOKEN");

		// When
		notificationService.sendCertificationTimeNotification();

		// Then
		verify(firebaseMessaging, times(3)).sendAsync(any(Message.class));
	}

	@DisplayName("특정 인증 시간에 해당하는 방 사용자들의 토큰값이 없을 때, - Void")
	@MethodSource("provideParticipants")
	@ParameterizedTest
	void notificationService_sendCertificationTimeNotification_NoFirebaseMessaging(List<Participant> participants) {
		// Given
		given(participantSearchRepository.findAllByRoomCertifyTime(any(Integer.class))).willReturn(participants);
		given(notificationRepository.findFcmTokenByMemberId(any(Long.class))).willReturn(null);

		// When
		notificationService.sendCertificationTimeNotification();

		// Then
		verify(firebaseMessaging, times(0)).sendAsync(any(Message.class));
	}

	static Stream<Arguments> provideParticipants() {
		Room room = RoomFixture.room(10);

		return Stream.of(Arguments.of(List.of(
			ParticipantFixture.participant(room, 1L),
			ParticipantFixture.participant(room, 2L),
			ParticipantFixture.participant(room, 3L)
		)));
	}
}
