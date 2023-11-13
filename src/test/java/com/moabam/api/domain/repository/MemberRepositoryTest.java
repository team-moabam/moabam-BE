package com.moabam.api.domain.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.moabam.api.domain.entity.Member;
import com.moabam.support.annotation.QuerydslRepositoryTest;
import com.moabam.support.fixture.MemberFixture;

@QuerydslRepositoryTest
class MemberRepositoryTest {

	@Autowired
	MemberRepository memberRepository;

	@DisplayName("")
	@Test
	void test() {
		// given
		Member member = MemberFixture.member();
		memberRepository.save(member);

		// when
		Member savedMember = memberRepository.findBySocialId(member.getSocialId()).orElse(null);

		// then
		Assertions.assertThat(savedMember).isNotNull();

	}
}
