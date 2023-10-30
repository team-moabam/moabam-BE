package com.moabam.factory;

import com.moabam.api.domain.entity.Bug;
import com.moabam.api.domain.entity.Member;

public final class MemberFactory {

	public static Member create(Long memberId) {
		Bug bug = Bug.builder()
			.morningBug(10)
			.nightBug(20)
			.goldenBug(30)
			.build();

		return Member.builder()
			.id(memberId)
			.bug(bug)
			.build();
	}
}
