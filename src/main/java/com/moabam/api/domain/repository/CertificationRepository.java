package com.moabam.api.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moabam.api.domain.entity.Certification;

public interface CertificationRepository extends JpaRepository<Certification, Long> {

}
