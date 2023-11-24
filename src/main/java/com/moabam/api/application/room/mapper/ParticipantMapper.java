package com.moabam.api.application.room.mapper;

import com.moabam.api.domain.member.Member;
import com.moabam.api.domain.room.Participant;
import com.moabam.api.domain.room.Room;
import com.moabam.api.dto.room.ParticipantResponse;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ParticipantMapper {

	public static Participant toParticipant(Room room, Long memberId) {
		return Participant.builder()
			.room(room)
			.memberId(memberId)
			.build();
	}

	public static ParticipantResponse toParticipantResponse(Member member, int contributionPoint) {
		return ParticipantResponse.builder()
			.memberId(member.getId())
			.nickname(member.getNickname())
			.contributionPoint(contributionPoint)
			.profileImage(member.getProfileImage())
			.build();
	}
}
