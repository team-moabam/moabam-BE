package com.moabam.api.domain.repository;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.moabam.api.domain.entity.Participant;
import com.moabam.api.domain.entity.Room;
import com.moabam.global.config.JpaConfig;
import com.moabam.support.fixture.ParticipantFixture;
import com.moabam.support.fixture.RoomFixture;

@DataJpaTest
@Import({JpaConfig.class, ParticipantSearchRepository.class})
class ParticipantSearchRepositoryTest {

	@Autowired
	private ParticipantSearchRepository participantSearchRepository;

	@Autowired
	private ParticipantRepository participantRepository;

	@Autowired
	private RoomRepository roomRepository;

	@DisplayName("인증 시간에 따른 참여자 조회를 성공적으로 했을 때, - List<Participant>")
	@MethodSource("provideParticipants")
	@ParameterizedTest
	void participantSearchRepository_findAllByRoomCertifyTime(Room room, List<Participant> participants) {
		// Given
		roomRepository.save(room);
		participantRepository.saveAll(participants);

		// When
		List<Participant> actual = participantSearchRepository.findAllByRoomCertifyTime(10);

		// Then
		assertThat(actual).hasSize(3);
	}

	static Stream<Arguments> provideParticipants() {
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
