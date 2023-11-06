package com.moabam.api.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moabam.api.domain.entity.DailyMemberCertification;

public interface DailyMemberCertificationRepository extends JpaRepository<DailyMemberCertification, Long> {

}
