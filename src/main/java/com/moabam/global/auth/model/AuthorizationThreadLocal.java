package com.moabam.global.auth.model;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AuthorizationThreadLocal {

	private static final ThreadLocal<AuthMember> authMember;

	static {
		authMember = new ThreadLocal<>();
	}

	public static void setAuthMember(AuthMember authMember) {
		AuthorizationThreadLocal.authMember.set(authMember);
	}

	public static AuthMember getAuthMember() {
		return authMember.get();
	}

	public static void remove() {
		authMember.remove();
	}
}
