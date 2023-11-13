package com.moabam.api.domain.room.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moabam.api.domain.room.DailyMemberCertification;

public interface DailyMemberCertificationRepository extends JpaRepository<DailyMemberCertification, Long> {

}
