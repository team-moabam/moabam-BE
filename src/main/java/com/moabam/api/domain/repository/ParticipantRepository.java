package com.moabam.api.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moabam.api.domain.entity.Participant;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {

}
