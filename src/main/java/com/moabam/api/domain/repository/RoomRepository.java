package com.moabam.api.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moabam.api.domain.entity.Room;

public interface RoomRepository extends JpaRepository<Room, Long> {

}
