package com.moabam.global.common.util;

import com.moabam.api.dto.AuthorizationMember;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AuthorizationThreadLocal {

	private static final ThreadLocal<AuthorizationMember> authorizationMember;

	static {
		authorizationMember = new ThreadLocal<>();
	}

	public static void setAuthorizationMember(AuthorizationMember authorizationMember) {
		AuthorizationThreadLocal.authorizationMember.set(authorizationMember);
	}

	public static AuthorizationMember getAuthorizationMember() {
		return authorizationMember.get();
	}

	public static void remove() {
		authorizationMember.remove();
	}
}
