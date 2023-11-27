package com.moabam.support.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.moabam.support.config.TestQuerydslConfig;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(TestQuerydslConfig.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
public @interface QuerydslRepositoryTest {

}
