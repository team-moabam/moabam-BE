package com.moabam.support.fixture;

import com.moabam.api.domain.member.Member;
import com.moabam.global.auth.model.AuthMember;

public final class AuthMemberFixture {

	public static AuthMember authMember(Member member) {
		return new AuthMember(member.getId(), member.getNickname(), member.getRole());
	}
}
