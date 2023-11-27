package com.moabam.api.dto.report;

import jakarta.validation.constraints.NotNull;

public record ReportRequest(
	@NotNull Long reportedId,
	Long roomId,
	Long certificationId,
	String description
) {

}
