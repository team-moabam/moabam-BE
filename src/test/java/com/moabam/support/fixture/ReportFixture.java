package com.moabam.support.fixture;

import com.moabam.api.domain.report.Report;
import com.moabam.api.domain.room.Certification;
import com.moabam.api.domain.room.Room;
import com.moabam.api.dto.report.ReportRequest;

public class ReportFixture {

	private static Long reportedId = 99L;
	private static Long roomId = 1L;
	private static Long certificationId = 1L;

	public static Report report(Room room, Certification certification) {
		return Report.builder()
			.reporterId(1L)
			.reportedMemberId(2L)
			.room(room)
			.certification(certification)
			.build();
	}

	public static ReportRequest reportRequest() {
		return new ReportRequest(reportedId, roomId, certificationId, "description");
	}
}
