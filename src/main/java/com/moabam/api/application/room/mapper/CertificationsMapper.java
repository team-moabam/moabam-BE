package com.moabam.api.application.room.mapper;

import java.time.LocalDate;
import java.util.List;

import com.moabam.api.domain.bug.BugType;
import com.moabam.api.domain.member.Member;
import com.moabam.api.domain.room.Certification;
import com.moabam.api.domain.room.DailyMemberCertification;
import com.moabam.api.domain.room.DailyRoomCertification;
import com.moabam.api.domain.room.Participant;
import com.moabam.api.domain.room.Room;
import com.moabam.api.domain.room.Routine;
import com.moabam.api.dto.room.CertificationImageResponse;
import com.moabam.api.dto.room.CertificationImagesResponse;
import com.moabam.api.dto.room.CertifiedMemberInfo;
import com.moabam.api.dto.room.TodayCertificateRankResponse;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CertificationsMapper {

	public static CertificationImageResponse toCertificateImageResponse(Long routineId, String image) {
		return CertificationImageResponse.builder()
			.routineId(routineId)
			.image(image)
			.build();
	}

	public static CertificationImagesResponse toCertificateImagesResponse(List<CertificationImageResponse> images) {
		return CertificationImagesResponse.builder()
			.images(images)
			.build();
	}

	public static TodayCertificateRankResponse toTodayCertificateRankResponse(int rank, Member member,
		int contributionPoint, String awakeImage, String sleepImage,
		CertificationImagesResponse certificationImagesResponses, boolean isNotificationSent) {

		return TodayCertificateRankResponse.builder()
			.rank(rank)
			.memberId(member.getId())
			.nickname(member.getNickname())
			.isNotificationSent(isNotificationSent)
			.profileImage(member.getProfileImage())
			.contributionPoint(contributionPoint)
			.awakeImage(awakeImage)
			.sleepImage(sleepImage)
			.certificationImage(certificationImagesResponses)
			.build();
	}

	public static DailyMemberCertification toDailyMemberCertification(Long memberId, Long roomId,
		Participant participant) {
		return DailyMemberCertification.builder()
			.memberId(memberId)
			.roomId(roomId)
			.participant(participant)
			.build();
	}

	public static DailyRoomCertification toDailyRoomCertification(Long roomId, LocalDate today) {
		return DailyRoomCertification.builder()
			.roomId(roomId)
			.certifiedAt(today)
			.build();
	}

	public static Certification toCertification(Routine routine, Long memberId, String image) {
		return Certification.builder()
			.routine(routine)
			.memberId(memberId)
			.image(image)
			.build();
	}

	public static CertifiedMemberInfo toCertifiedMemberInfo(LocalDate date, BugType bugType, Room room, Member member) {
		return CertifiedMemberInfo.builder()
			.date(date)
			.bugType(bugType)
			.room(room)
			.member(member)
			.build();
	}
}
