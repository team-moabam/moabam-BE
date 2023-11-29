package com.moabam.api.presentation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moabam.api.domain.member.Member;
import com.moabam.api.domain.member.repository.MemberRepository;
import com.moabam.api.domain.room.Certification;
import com.moabam.api.domain.room.Room;
import com.moabam.api.domain.room.Routine;
import com.moabam.api.domain.room.repository.CertificationRepository;
import com.moabam.api.domain.room.repository.RoomRepository;
import com.moabam.api.domain.room.repository.RoutineRepository;
import com.moabam.api.dto.report.ReportRequest;
import com.moabam.support.annotation.WithMember;
import com.moabam.support.common.WithoutFilterSupporter;
import com.moabam.support.fixture.MemberFixture;
import com.moabam.support.fixture.ReportFixture;
import com.moabam.support.fixture.RoomFixture;

import jakarta.persistence.EntityManagerFactory;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ReportControllerTest extends WithoutFilterSupporter {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	MemberRepository memberRepository;

	@Autowired
	RoomRepository roomRepository;

	@Autowired
	CertificationRepository certificationRepository;

	@Autowired
	RoutineRepository routineRepository;

	@Autowired
	EntityManagerFactory entityManagerFactory;

	Member reportedMember;

	@BeforeAll
	void setUp() {
		reportedMember = MemberFixture.member();
		memberRepository.save(reportedMember);
	}

	@DisplayName("방이나 인증 하나 신고")
	@WithMember
	@ParameterizedTest
	@CsvSource({"true, false", "false, true", "true, true"})
	void reports_success(boolean roomFilter, boolean certificationFilter) throws Exception {
		// given
		String content = "내용";
		Room room = RoomFixture.room();
		Routine routine = RoomFixture.routine(room, content);
		Certification certification = RoomFixture.certification(routine);
		roomRepository.save(room);
		routineRepository.save(routine);
		certificationRepository.save(certification);

		Long roomId = null;
		Long certificationId = null;

		if (roomFilter) {
			roomId = room.getId();
		}
		if (certificationFilter) {
			certificationId = certification.getId();
		}

		ReportRequest reportRequest = ReportFixture.reportRequest(reportedMember.getId(), roomId, certificationId);
		String request = objectMapper.writeValueAsString(reportRequest);

		// expected
		mockMvc.perform(post("/reports")
				.contentType(MediaType.APPLICATION_JSON)
				.content(request))
			.andExpect(status().is2xxSuccessful());
	}

	@DisplayName("사용자 신고 성공")
	@WithMember
	@Test
	void reports_failBy_subject_null() throws Exception {
		// given
		Member member = MemberFixture.member("2", "ji");
		memberRepository.save(member);

		ReportRequest reportRequest = ReportFixture.reportRequest(member.getId(), null, null);
		String request = objectMapper.writeValueAsString(reportRequest);

		// expected
		mockMvc.perform(post("/reports")
				.contentType(MediaType.APPLICATION_JSON)
				.content(request))
			.andExpect(status().is2xxSuccessful());
	}

	@DisplayName("회원 조회 실패로 신고 실패")
	@WithMember
	@Test
	void reports_failBy_member() throws Exception {
		// given
		Member newMember = MemberFixture.member("9999", "n");
		memberRepository.save(newMember);

		newMember.delete(LocalDateTime.now());
		memberRepository.flush();
		memberRepository.delete(newMember);
		memberRepository.flush();

		ReportRequest reportRequest = ReportFixture.reportRequest(newMember.getId(), 1L, 1L);
		String request = objectMapper.writeValueAsString(reportRequest);

		// expected
		mockMvc.perform(post("/reports")
				.contentType(MediaType.APPLICATION_JSON)
				.content(request))
			.andExpect(status().is4xxClientError());
	}

	@DisplayName("방이나 인증 하나 신고 실패")
	@WithMember
	@ParameterizedTest
	@CsvSource({"12394,", ",123415", "12394, 123415"})
	void reports_failBy_room_certification(Long roomId, Long certificationId) throws Exception {
		// given
		ReportRequest reportRequest = ReportFixture.reportRequest(reportedMember.getId(), roomId,
			certificationId);
		String request = objectMapper.writeValueAsString(reportRequest);

		// expected
		mockMvc.perform(post("/reports")
				.contentType(MediaType.APPLICATION_JSON)
				.content(request))
			.andExpect(status().is4xxClientError());
	}
}
