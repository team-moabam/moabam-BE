package com.moabam.support.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import com.moabam.api.domain.repository.InventorySearchRepository;
import com.moabam.api.domain.repository.ItemSearchRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@EnableJpaAuditing
@TestConfiguration
public class TestQueryDslConfig {

	@PersistenceContext
	private EntityManager entityManager;

	@Bean
	public JPAQueryFactory jpaQueryFactory() {
		return new JPAQueryFactory(entityManager);
	}

	@Bean
	public ItemSearchRepository itemSearchRepository() {
		return new ItemSearchRepository(jpaQueryFactory());
	}

	@Bean
	public InventorySearchRepository inventorySearchRepository() {
		return new InventorySearchRepository(jpaQueryFactory());
	}
}
