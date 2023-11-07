package com.moabam.support.fixture;

import com.moabam.api.domain.entity.Participant;
import com.moabam.api.domain.entity.Room;

public final class ParticipantFixture {

	public static Participant participant(Room room, Long memberId) {
		return Participant.builder()
			.room(room)
			.memberId(memberId)
			.build();
	}
}
