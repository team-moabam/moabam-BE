package com.moabam.api.domain.member.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moabam.api.domain.member.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {

	Optional<Member> findBySocialId(String id);

	Optional<Member> findByNickname(String nickname);

	boolean existsByNickname(String nickname);
}
