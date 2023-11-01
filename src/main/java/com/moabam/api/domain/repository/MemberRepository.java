package com.moabam.api.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moabam.api.domain.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {

}
