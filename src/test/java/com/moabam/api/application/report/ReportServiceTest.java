package com.moabam.api.application.report;

import static com.moabam.global.error.model.ErrorMessage.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.moabam.api.application.member.MemberService;
import com.moabam.api.application.room.CertificationService;
import com.moabam.api.application.room.RoomService;
import com.moabam.api.domain.member.Member;
import com.moabam.api.domain.report.repository.ReportRepository;
import com.moabam.api.domain.room.Certification;
import com.moabam.api.domain.room.Room;
import com.moabam.api.domain.room.Routine;
import com.moabam.api.dto.report.ReportRequest;
import com.moabam.global.auth.model.AuthMember;
import com.moabam.global.error.exception.BadRequestException;
import com.moabam.support.annotation.WithMember;
import com.moabam.support.common.FilterProcessExtension;
import com.moabam.support.fixture.MemberFixture;
import com.moabam.support.fixture.ReportFixture;
import com.moabam.support.fixture.RoomFixture;

@ExtendWith({MockitoExtension.class, FilterProcessExtension.class})
public class ReportServiceTest {

	@InjectMocks
	ReportService reportService;

	@Mock
	CertificationService certificationService;

	@Mock
	RoomService roomService;

	@Mock
	MemberService memberService;

	@Mock
	ReportRepository reportRepository;

	@DisplayName("신고 대상이 없어서 실패")
	@Test
	void no_report_subject_fail(@WithMember AuthMember authMember) {
		// given
		ReportRequest reportRequest = new ReportRequest(5L, null, null, "st");

		// When + Then
		assertThatThrownBy(() -> reportService.report(authMember, reportRequest))
			.isInstanceOf(BadRequestException.class)
			.hasMessage(REPORT_REQUEST_ERROR.getMessage());
	}

	@DisplayName("신고 성공")
	@Test
	void report_success(@WithMember AuthMember authMember) {
		// given
		Room room = RoomFixture.room();
		Routine routine = RoomFixture.routine(room, "ets");
		Certification certification = RoomFixture.certification(routine);
		ReportRequest reportRequest = ReportFixture.reportRequest();
		Member member = spy(MemberFixture.member());

		given(member.getId()).willReturn(authMember.id());
		given(memberService.findMember(reportRequest.reportedId())).willReturn(member);
		given(roomService.findRoom(reportRequest.roomId())).willReturn(RoomFixture.room());
		given(certificationService.findCertification(reportRequest.certificationId()))
			.willReturn(certification);

		// When + Then
		assertThatNoException()
			.isThrownBy(() -> reportService.report(authMember, reportRequest));
	}
}
