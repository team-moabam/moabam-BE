package com.moabam.api.domain.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moabam.api.domain.member.Badge;

public interface BadgeRepository extends JpaRepository<Badge, Long> {

}
