package com.moabam.api.domain.room.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moabam.api.domain.room.Routine;

public interface RoutineRepository extends JpaRepository<Routine, Long> {

}
