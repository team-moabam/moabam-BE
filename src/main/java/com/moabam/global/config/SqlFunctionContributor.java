package com.moabam.global.config;

import static org.hibernate.type.StandardBasicTypes.*;

import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.boot.model.FunctionContributor;

public class SqlFunctionContributor implements FunctionContributor {

	@Override
	public void contributeFunctions(FunctionContributions functionContributions) {
		functionContributions.getFunctionRegistry()
			.registerPattern(
				"match_against", "MATCH (?1) AGAINST (?2 IN NATURAL LANGUAGE MODE)",
				functionContributions.getTypeConfiguration().getBasicTypeRegistry().resolve(DOUBLE));

		functionContributions.getFunctionRegistry()
			.registerPattern(
				"match_against_two", "MATCH (?1, ?2) AGAINST (?3 IN NATURAL LANGUAGE MODE)",
				functionContributions.getTypeConfiguration().getBasicTypeRegistry().resolve(DOUBLE));
	}
}
