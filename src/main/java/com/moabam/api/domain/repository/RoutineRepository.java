package com.moabam.api.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moabam.api.domain.entity.Routine;

public interface RoutineRepository extends JpaRepository<Routine, Long> {

}
