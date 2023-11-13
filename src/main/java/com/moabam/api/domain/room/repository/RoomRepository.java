package com.moabam.api.domain.room.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moabam.api.domain.room.Room;

public interface RoomRepository extends JpaRepository<Room, Long> {

}
