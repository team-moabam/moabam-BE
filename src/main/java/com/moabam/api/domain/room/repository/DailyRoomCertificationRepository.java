package com.moabam.api.domain.room.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moabam.api.domain.room.DailyRoomCertification;

public interface DailyRoomCertificationRepository extends JpaRepository<DailyRoomCertification, Long> {

}
