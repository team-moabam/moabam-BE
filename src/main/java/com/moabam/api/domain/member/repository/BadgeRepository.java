package com.moabam.api.domain.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moabam.api.domain.member.Badge;
import com.moabam.api.domain.member.BadgeType;

public interface BadgeRepository extends JpaRepository<Badge, Long> {

	boolean existsByMemberIdAndType(Long memberId, BadgeType type);

}
