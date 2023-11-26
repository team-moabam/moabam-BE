package com.moabam.api.application.notification;

import static com.moabam.global.common.util.GlobalConstant.*;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.moabam.api.application.room.RoomService;
import com.moabam.api.domain.notification.repository.NotificationRepository;
import com.moabam.api.domain.room.Participant;
import com.moabam.api.domain.room.repository.ParticipantSearchRepository;
import com.moabam.api.infrastructure.fcm.FcmService;
import com.moabam.global.auth.model.AuthMember;
import com.moabam.global.common.util.ClockHolder;
import com.moabam.global.error.exception.ConflictException;
import com.moabam.global.error.model.ErrorMessage;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

	private static final String KNOCK_BODY = "%s님이 콕 찔렀습니다.";
	private static final String CERTIFY_TIME_BODY = "%s방 인증 시간입니다.";

	private final ClockHolder clockHolder;
	private final FcmService fcmService;
	private final RoomService roomService;

	private final NotificationRepository notificationRepository;
	private final ParticipantSearchRepository participantSearchRepository;

	@Transactional
	public void sendKnock(AuthMember member, Long targetId, Long roomId) {
		roomService.validateRoomById(roomId);
		validateConflictKnock(member.id(), targetId, roomId);

		String fcmToken = fcmService.findTokenByMemberId(targetId);
		fcmService.sendAsync(fcmToken, String.format(KNOCK_BODY, member.nickname()));
		notificationRepository.saveKnock(member.id(), targetId, roomId);
	}

	@Scheduled(cron = "0 50 * * * *")
	public void sendCertificationTime() {
		int certificationTime = (clockHolder.times().getHour() + ONE_HOUR) % HOURS_IN_A_DAY;
		List<Participant> participants = participantSearchRepository.findAllByRoomCertifyTime(certificationTime);

		participants.parallelStream().forEach(participant -> {
			String roomTitle = participant.getRoom().getTitle();
			String notificationBody = String.format(CERTIFY_TIME_BODY, roomTitle);
			String fcmToken = fcmService.findTokenByMemberId(participant.getMemberId());
			fcmService.sendAsync(fcmToken, notificationBody);
		});
	}

	public List<Long> getMyKnockStatusInRoom(Long memberId, Long roomId, List<Participant> participants) {
		List<Participant> filteredParticipants = participants.stream()
			.filter(participant -> !participant.getMemberId().equals(memberId))
			.toList();

		Predicate<Long> knockPredicate = targetId ->
			notificationRepository.existsKnockByKey(memberId, targetId, roomId);

		Map<Boolean, List<Long>> knockStatus = filteredParticipants.stream()
			.map(Participant::getMemberId)
			.collect(Collectors.partitioningBy(knockPredicate));

		return knockStatus.get(true);
	}

	private void validateConflictKnock(Long memberId, Long targetId, Long roomId) {
		if (notificationRepository.existsKnockByKey(memberId, targetId, roomId)) {
			throw new ConflictException(ErrorMessage.CONFLICT_KNOCK);
		}
	}
}
