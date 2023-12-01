package com.moabam.api.domain.room.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moabam.api.domain.room.Participant;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {

	List<Participant> findAllByMemberId(Long id);
}
