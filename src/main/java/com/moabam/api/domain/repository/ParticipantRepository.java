package com.moabam.api.domain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moabam.api.domain.entity.Participant;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {

	Optional<Participant> findParticipantByRoomIdAndMemberId(Long roomId, Long MemberId);
}
