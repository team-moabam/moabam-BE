package com.moabam.api.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.moabam.api.application.notification.NotificationService;
import com.moabam.api.application.room.RoomService;
import com.moabam.api.domain.room.Participant;
import com.moabam.api.domain.room.repository.ParticipantSearchRepository;
import com.moabam.api.dto.notification.KnockNotificationStatusResponse;
import com.moabam.api.infrastructure.redis.NotificationRepository;
import com.moabam.global.auth.annotation.MemberTest;
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

	@DisplayName("성공적으로 상대에게 콕 알림을 보낸다. - Void")
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

	@DisplayName("콕 찌를 상대의 방이 존재하지 않는다. - NotFoundException")
	@Test
	void notificationService_sendKnockNotification_Room_NotFoundException() {
		// Given
		willThrow(NotFoundException.class).given(roomService).validateRoomById(any(Long.class));

		// When & Then
		assertThatThrownBy(() -> notificationService.sendKnockNotification(memberTest, 1L, 1L))
			.isInstanceOf(NotFoundException.class);
	}

	@DisplayName("콕 찌를 상대의 FCM 토큰이 존재하지 않는다. - NotFoundException")
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

	@DisplayName("콕 찌를 상대가 이미 찌른 상대이다. - ConflictException")
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

	@DisplayName("특정 인증 시간에 해당하는 방 사용자들에게 알림을 성공적으로 보낸다. - Void")
	@MethodSource("com.moabam.support.fixture.ParticipantFixture#provideParticipants")
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

	@DisplayName("특정 인증 시간에 해당하는 방 사용자들의 토큰값이 없다. - Void")
	@MethodSource("com.moabam.support.fixture.ParticipantFixture#provideParticipants")
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

	@DisplayName("특정 방에서 나 이외의 모든 사용자에게 콕 알림을 보낸다. - KnockNotificationStatusResponse")
	@MethodSource("com.moabam.support.fixture.ParticipantFixture#provideParticipants")
	@ParameterizedTest
	void notificationService_knocked_checkMyKnockNotificationStatusInRoom(List<Participant> participants) {
		// Given
		given(participantSearchRepository.findOtherParticipantsInRoom(any(Long.class), any(Long.class)))
			.willReturn(participants);
		given(notificationRepository.existsByKey(any(String.class))).willReturn(true);

		// When
		KnockNotificationStatusResponse actual =
			notificationService.checkMyKnockNotificationStatusInRoom(memberTest, 1L);

		// Then
		assertThat(actual.knockedMembersId()).hasSize(3);
		assertThat(actual.notKnockedMembersId()).isEmpty();
	}

	@DisplayName("특정 방에서 나 이외의 모든 사용자에게 콕 알림을 보낸 적이 없다. - KnockNotificationStatusResponse")
	@MethodSource("com.moabam.support.fixture.ParticipantFixture#provideParticipants")
	@ParameterizedTest
	void notificationService_notKnocked_checkMyKnockNotificationStatusInRoom(List<Participant> participants) {
		// Given
		given(participantSearchRepository.findOtherParticipantsInRoom(any(Long.class), any(Long.class)))
			.willReturn(participants);
		given(notificationRepository.existsByKey(any(String.class))).willReturn(false);

		// When
		KnockNotificationStatusResponse actual =
			notificationService.checkMyKnockNotificationStatusInRoom(memberTest, 1L);

		// Then
		assertThat(actual.knockedMembersId()).isEmpty();
		assertThat(actual.notKnockedMembersId()).hasSize(3);
	}
}
