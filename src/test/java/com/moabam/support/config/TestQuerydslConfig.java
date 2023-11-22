package com.moabam.support.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import com.moabam.api.domain.item.repository.InventorySearchRepository;
import com.moabam.api.domain.item.repository.ItemSearchRepository;
import com.moabam.api.domain.member.repository.MemberSearchRepository;
import com.moabam.api.domain.room.repository.CertificationsSearchRepository;
import com.querydsl.jpa.JPQLTemplates;
import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@EnableJpaAuditing
@TestConfiguration
public class TestQuerydslConfig {

	@PersistenceContext
	private EntityManager entityManager;

	@Bean
	public JPAQueryFactory jpaQueryFactory() {
		return new JPAQueryFactory(JPQLTemplates.DEFAULT, entityManager);
	}

	@Bean
	public ItemSearchRepository itemSearchRepository() {
		return new ItemSearchRepository(jpaQueryFactory());
	}

	@Bean
	public InventorySearchRepository inventorySearchRepository() {
		return new InventorySearchRepository(jpaQueryFactory());
	}

	@Bean
	public CertificationsSearchRepository certificationsSearchRepository() {
		return new CertificationsSearchRepository(jpaQueryFactory());
	}

	@Bean
	public MemberSearchRepository memberSearchRepository() {
		return new MemberSearchRepository(jpaQueryFactory());
	}
}
