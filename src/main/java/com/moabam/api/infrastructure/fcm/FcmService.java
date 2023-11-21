package com.moabam.api.infrastructure.fcm;

import org.springframework.stereotype.Service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.moabam.global.auth.model.AuthorizationMember;
import com.moabam.global.error.exception.NotFoundException;
import com.moabam.global.error.model.ErrorMessage;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FcmService {

	private final FirebaseMessaging firebaseMessaging;
	private final FcmRepository fcmRepository;

	// TODO : 세연님 로그인 시, 해당 메서드 사용해서 해당 유저의 FCM TOKEN 저장하면 됩니다. Front와 상의 후 삭제예정
	public void createToken(AuthorizationMember member, String fcmToken) {
		if (fcmToken == null || fcmToken.isBlank()) {
			return;
		}

		fcmRepository.saveToken(member.id(), fcmToken);
	}

	// TODO : 세연님 로그아웃 시, 해당 메서드 사용해서 해당 유저의 FCM TOKEN 삭제하시면 됩니다. (이 코드는 원하시면 변경하셔도 됩니다.)
	public void deleteTokenByMemberId(Long memberId) {
		fcmRepository.deleteTokenByMemberId(memberId);
	}

	public String findTokenByMemberId(Long targetId) {
		validateToken(targetId);

		return fcmRepository.findTokenByMemberId(targetId);
	}

	public void sendAsync(String fcmToken, String notificationBody) {
		Notification notification = FcmMapper.toNotification(notificationBody);

		if (fcmToken != null) {
			Message message = FcmMapper.toMessage(notification, fcmToken);
			firebaseMessaging.sendAsync(message);
		}
	}

	private void validateToken(Long memberId) {
		if (!fcmRepository.existsTokenByMemberId(memberId)) {
			throw new NotFoundException(ErrorMessage.NOT_FOUND_FCM_TOKEN);
		}
	}
}
