package com.moabam.support.fixture;

import com.moabam.api.dto.member.DeleteMemberResponse;

public final class DeleteMemberFixture {

	public static DeleteMemberResponse deleteMemberResponse() {
		return DeleteMemberResponse.builder()
			.id(1L)
			.socialId("1")
			.build();
	}
}
