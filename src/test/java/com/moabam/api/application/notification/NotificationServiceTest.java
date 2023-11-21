package com.moabam.api.application.notification;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.moabam.api.application.room.RoomService;
import com.moabam.api.domain.notification.repository.NotificationRepository;
import com.moabam.api.domain.room.Participant;
import com.moabam.api.domain.room.repository.ParticipantSearchRepository;
import com.moabam.api.infrastructure.fcm.FcmService;
import com.moabam.global.auth.model.AuthorizationMember;
import com.moabam.global.auth.model.AuthorizationThreadLocal;
import com.moabam.global.common.util.ClockHolder;
import com.moabam.global.error.exception.ConflictException;
import com.moabam.global.error.exception.NotFoundException;
import com.moabam.global.error.model.ErrorMessage;
import com.moabam.support.annotation.WithMember;
import com.moabam.support.common.FilterProcessExtension;

@ExtendWith({MockitoExtension.class, FilterProcessExtension.class})
class NotificationServiceTest {

	@InjectMocks
	private NotificationService notificationService;

	@Mock
	private RoomService roomService;

	@Mock
	private FcmService fcmService;

	@Mock
	private NotificationRepository notificationRepository;

	@Mock
	private ParticipantSearchRepository participantSearchRepository;

	@Mock
	private ClockHolder clockHolder;

	@WithMember
	@DisplayName("성공적으로 상대에게 콕 알림을 보낸다. - Void")
	@Test
	void notificationService_sendKnockNotification() {
		// Given
		AuthorizationMember member = AuthorizationThreadLocal.getAuthorizationMember();

		willDoNothing().given(roomService).validateRoomById(any(Long.class));
		given(notificationRepository.existsFcmTokenByMemberId(any(Long.class))).willReturn(true);
		given(notificationRepository.existsKnockByKnockKey(any(String.class))).willReturn(false);
		given(notificationRepository.findFcmTokenByMemberId(any(Long.class))).willReturn("FCM-TOKEN");

		// When
		notificationService.sendKnock(member, 2L, 1L);

		// Then
		verify(fcmService).sendAsync(any(String.class), any(String.class));
		verify(notificationRepository).saveKnock(any(String.class));
	}

	@WithMember
	@DisplayName("콕 찌를 상대의 방이 존재하지 않는다. - NotFoundException")
	@Test
	void notificationService_sendKnockNotification_Room_NotFoundException() {
		// Given
		AuthorizationMember member = AuthorizationThreadLocal.getAuthorizationMember();
		willThrow(NotFoundException.class).given(roomService).validateRoomById(any(Long.class));

		// When & Then
		assertThatThrownBy(() -> notificationService.sendKnock(member, 1L, 1L))
			.isInstanceOf(NotFoundException.class);
	}

	@WithMember
	@DisplayName("콕 찌를 상대의 FCM 토큰이 존재하지 않는다. - NotFoundException")
	@Test
	void notificationService_sendKnockNotification_FcmToken_NotFoundException() {
		// Given
		AuthorizationMember member = AuthorizationThreadLocal.getAuthorizationMember();

		willDoNothing().given(roomService).validateRoomById(any(Long.class));
		given(notificationRepository.existsKnockByKnockKey(any(String.class))).willReturn(false);
		given(notificationRepository.existsFcmTokenByMemberId(any(Long.class))).willReturn(false);

		// When & Then
		assertThatThrownBy(() -> notificationService.sendKnock(member, 1L, 1L))
			.isInstanceOf(NotFoundException.class)
			.hasMessage(ErrorMessage.NOT_FOUND_FCM_TOKEN.getMessage());
	}

	@WithMember
	@DisplayName("콕 찌를 상대가 이미 찌른 상대이다. - ConflictException")
	@Test
	void notificationService_sendKnockNotification_ConflictException() {
		// Given
		AuthorizationMember member = AuthorizationThreadLocal.getAuthorizationMember();

		willDoNothing().given(roomService).validateRoomById(any(Long.class));
		given(notificationRepository.existsKnockByKnockKey(any(String.class))).willReturn(true);

		// When & Then
		assertThatThrownBy(() -> notificationService.sendKnock(member, 1L, 1L))
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
		given(clockHolder.times()).willReturn(LocalDateTime.now());

		// When
		notificationService.sendCertificationTime();

		// Then
		verify(fcmService, times(3)).sendAsync(any(String.class), any(String.class));
	}

	@WithMember
	@DisplayName("특정 인증 시간에 해당하는 방 사용자들의 토큰값이 없다. - Void")
	@MethodSource("com.moabam.support.fixture.ParticipantFixture#provideParticipants")
	@ParameterizedTest
	void notificationService_sendCertificationTimeNotification_NoFirebaseMessaging(List<Participant> participants) {
		// Given
		given(participantSearchRepository.findAllByRoomCertifyTime(any(Integer.class))).willReturn(participants);
		given(notificationRepository.findFcmTokenByMemberId(any(Long.class))).willReturn(null);
		given(clockHolder.times()).willReturn(LocalDateTime.now());

		// When
		notificationService.sendCertificationTime();

		// Then
		verify(fcmService, times(0)).sendAsync(any(String.class), any(String.class));
	}

	@WithMember
	@DisplayName("특정 방에서 나 이외의 모든 사용자에게 콕 알림을 보낸다. - KnockNotificationStatusResponse")
	@MethodSource("com.moabam.support.fixture.ParticipantFixture#provideParticipants")
	@ParameterizedTest
	void notificationService_knocked_checkMyKnockNotificationStatusInRoom(List<Participant> participants) {
		// Given
		AuthorizationMember member = AuthorizationThreadLocal.getAuthorizationMember();

		given(notificationRepository.existsKnockByKnockKey(any(String.class))).willReturn(true);

		// When
		List<Long> actual =
			notificationService.getMyKnockStatusInRoom(member.id(), 1L, participants);

		// Then
		assertThat(actual).hasSize(2);
	}

	@WithMember
	@DisplayName("특정 방에서 나 이외의 모든 사용자에게 콕 알림을 보낸 적이 없다. - KnockNotificationStatusResponse")
	@MethodSource("com.moabam.support.fixture.ParticipantFixture#provideParticipants")
	@ParameterizedTest
	void notificationService_notKnocked_checkMyKnockNotificationStatusInRoom(List<Participant> participants) {
		// Given
		AuthorizationMember member = AuthorizationThreadLocal.getAuthorizationMember();

		// given
		given(notificationRepository.existsKnockByKnockKey(any(String.class))).willReturn(false);

		// When
		List<Long> actual =
			notificationService.getMyKnockStatusInRoom(member.id(), 1L, participants);

		// Then
		assertThat(actual).isEmpty();
	}
}
