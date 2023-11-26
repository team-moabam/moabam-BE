package com.moabam.api.domain.room.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moabam.api.domain.room.Routine;

public interface RoutineRepository extends JpaRepository<Routine, Long> {

	List<Routine> findAllByRoomId(Long roomId);

	List<Routine> findAllByRoomIdIn(List<Long> roomIds);
}
