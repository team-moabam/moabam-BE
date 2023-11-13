package com.moabam.support.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.moabam.api.domain.entity.enums.Role;

@Target({ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface WithMember {

	long id() default 1L;

	String nickname() default "닉네임";

	Role role() default Role.USER;
}
