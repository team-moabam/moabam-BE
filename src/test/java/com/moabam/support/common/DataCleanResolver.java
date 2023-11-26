package com.moabam.support.common;

import java.util.List;

import javax.annotation.Nullable;

import org.springframework.boot.test.context.TestComponent;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

@TestComponent
public class DataCleanResolver {

	private EntityManager entityManager;

	public DataCleanResolver(@Nullable EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@Transactional
	public void clean() {
		if (entityManager == null) {
			return;
		}

		List<String> tableInfos = getTableInfos();
		doClean(tableInfos);
		entityManager.clear();
	}

	private List<String> getTableInfos() {
		List<Object[]> tableInfos = entityManager.createNativeQuery("show tables").getResultList();

		return tableInfos.stream()
			.map(tableInfo -> (String)tableInfo[0])
			.toList();
	}

	private void doClean(List<String> tableInfos) {
		setForeignKeyCheck(false);
		tableInfos.stream()
			.map(tableInfo -> entityManager.createNativeQuery(
				String.format("TRUNCATE TABLE %s RESTART IDENTITY", tableInfo)))
			.forEach(Query::executeUpdate);
		setForeignKeyCheck(true);
	}

	private void setForeignKeyCheck(boolean data) {
		entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY " + data)
			.executeUpdate();
	}
}
