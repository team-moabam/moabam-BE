package com.moabam.api.domain.room.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moabam.api.domain.room.Participant;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {

}
