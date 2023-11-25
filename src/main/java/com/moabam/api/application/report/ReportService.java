package com.moabam.api.application.report;

import static java.util.Objects.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.moabam.api.application.member.MemberService;
import com.moabam.api.application.room.CertificationService;
import com.moabam.api.application.room.RoomService;
import com.moabam.api.domain.member.Member;
import com.moabam.api.domain.report.Report;
import com.moabam.api.domain.report.repository.ReportRepository;
import com.moabam.api.domain.room.Certification;
import com.moabam.api.domain.room.Room;
import com.moabam.api.dto.report.ReportRequest;
import com.moabam.global.auth.model.AuthMember;
import com.moabam.global.error.exception.BadRequestException;
import com.moabam.global.error.model.ErrorMessage;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportService {

	private final MemberService memberService;
	private final RoomService roomService;
	private final CertificationService certificationService;
	private final ReportRepository reportRepository;

	@Transactional
	public void report(AuthMember authMember, ReportRequest reportRequest) {
		validateNoReportSubject(reportRequest.roomId(), reportRequest.certificationId());
		Member reportedMember = memberService.findMember(reportRequest.reportedId());
		Room room = roomService.findRoom(reportRequest.roomId());
		Certification certification = certificationService.findCertification(reportRequest.certificationId());
		Report report = ReportMapper.toReport(authMember.id(), reportedMember.getId(),
			room, certification, reportRequest.description());
		reportRepository.save(report);
	}

	private void validateNoReportSubject(Long roomId, Long certificationId) {
		if (isNull(roomId) && isNull(certificationId)) {
			throw new BadRequestException(ErrorMessage.REPORT_REQUEST_ERROR);
		}
	}
}
