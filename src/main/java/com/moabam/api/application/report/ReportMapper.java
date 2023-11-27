package com.moabam.api.application.report;

import com.moabam.api.domain.report.Report;
import com.moabam.api.domain.room.Certification;
import com.moabam.api.domain.room.Room;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReportMapper {

	public static Report toReport(Long reporterId, Long reportedMemberId,
		Room room, Certification certification, String description) {
		return Report.builder()
			.reporterId(reporterId)
			.reportedMemberId(reportedMemberId)
			.certification(certification)
			.room(room)
			.description(description)
			.build();
	}
}
