package com.moabam.support.fixture;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.provider.Arguments;

import com.moabam.api.domain.entity.Participant;
import com.moabam.api.domain.entity.Room;

public final class ParticipantFixture {

	public static Participant participant(Room room, Long memberId) {
		return Participant.builder()
			.room(room)
			.memberId(memberId)
			.build();
	}

	public static Stream<Arguments> provideParticipants() {
		Room room = RoomFixture.room(10);

		return Stream.of(Arguments.of(List.of(
			ParticipantFixture.participant(room, 1L),
			ParticipantFixture.participant(room, 3L),
			ParticipantFixture.participant(room, 7L)
		)));
	}

	public static Stream<Arguments> provideRoomAndParticipants() {
		Room room = RoomFixture.room(10);

		return Stream.of(Arguments.of(
			room,
			List.of(
				ParticipantFixture.participant(room, 1L),
				ParticipantFixture.participant(room, 2L),
				ParticipantFixture.participant(room, 3L)
			))
		);
	}
}
