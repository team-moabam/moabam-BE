package com.moabam.api.domain.room.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moabam.api.domain.room.Certification;

public interface CertificationRepository extends JpaRepository<Certification, Long> {

}
