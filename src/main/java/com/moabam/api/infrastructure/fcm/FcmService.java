package com.moabam.api.infrastructure.fcm;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.moabam.global.auth.model.AuthMember;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FcmService {

	private final FirebaseMessaging firebaseMessaging;
	private final FcmRepository fcmRepository;

	// TODO : 세연님 로그인 시, 해당 메서드 사용해서 해당 유저의 FCM TOKEN 저장하면 됩니다. Front와 상의 후 삭제예정
	public void createToken(AuthMember authMember, String fcmToken) {
		if (fcmToken == null || fcmToken.isBlank()) {
			return;
		}

		fcmRepository.saveToken(authMember.id(), fcmToken);
	}

	// TODO : 세연님 로그아웃 시, 해당 메서드 사용해서 해당 유저의 FCM TOKEN 삭제하시면 됩니다. (이 코드는 원하시면 변경하셔도 됩니다.)
	public void deleteTokenByMemberId(Long memberId) {
		fcmRepository.deleteTokenByMemberId(memberId);
	}

	public Optional<String> findTokenByMemberId(Long targetId) {
		return Optional.ofNullable(fcmRepository.findTokenByMemberId(targetId));
	}

	public void sendAsync(String fcmToken, String notificationBody) {
		Notification notification = FcmMapper.toNotification(notificationBody);

		if (fcmToken != null) {
			Message message = FcmMapper.toMessage(notification, fcmToken);
			firebaseMessaging.sendAsync(message);
		}
	}
}
