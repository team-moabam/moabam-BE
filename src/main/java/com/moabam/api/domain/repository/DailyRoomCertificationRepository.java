package com.moabam.api.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moabam.api.domain.entity.DailyRoomCertification;

public interface DailyRoomCertificationRepository extends JpaRepository<DailyRoomCertification, Long> {

}
