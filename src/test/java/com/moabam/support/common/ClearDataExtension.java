package com.moabam.support.common;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

public class ClearDataExtension implements AfterAllCallback {

	@Override
	public void afterAll(ExtensionContext context) {
		DataCleanResolver dataCleanResolver =
			SpringExtension.getApplicationContext(context).getBean(DataCleanResolver.class);
		dataCleanResolver.clean();
	}
}
