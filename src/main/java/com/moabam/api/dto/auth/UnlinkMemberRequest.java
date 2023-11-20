package com.moabam.api.dto.auth;

public record UnlinkMemberRequest(
	String targetIdType,
	String targetId
) {

	public static UnlinkMemberRequest of(String targetId) {
		return new UnlinkMemberRequest("user_id", targetId);
	}
}
