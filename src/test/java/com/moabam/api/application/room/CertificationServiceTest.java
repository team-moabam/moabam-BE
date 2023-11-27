package com.moabam.api.application.room;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.moabam.api.application.image.ImageService;
import com.moabam.api.application.member.MemberService;
import com.moabam.api.application.room.mapper.CertificationsMapper;
import com.moabam.api.domain.bug.BugType;
import com.moabam.api.domain.member.Member;
import com.moabam.api.domain.room.DailyMemberCertification;
import com.moabam.api.domain.room.DailyRoomCertification;
import com.moabam.api.domain.room.Participant;
import com.moabam.api.domain.room.Room;
import com.moabam.api.domain.room.Routine;
import com.moabam.api.domain.room.repository.CertificationRepository;
import com.moabam.api.domain.room.repository.CertificationsSearchRepository;
import com.moabam.api.domain.room.repository.DailyMemberCertificationRepository;
import com.moabam.api.domain.room.repository.DailyRoomCertificationRepository;
import com.moabam.api.domain.room.repository.ParticipantRepository;
import com.moabam.api.domain.room.repository.ParticipantSearchRepository;
import com.moabam.api.domain.room.repository.RoomRepository;
import com.moabam.api.domain.room.repository.RoutineRepository;
import com.moabam.api.dto.room.CertifiedMemberInfo;
import com.moabam.global.common.util.ClockHolder;
import com.moabam.support.fixture.MemberFixture;
import com.moabam.support.fixture.RoomFixture;

@ExtendWith(MockitoExtension.class)
class CertificationServiceTest {

	@InjectMocks
	private CertificationService certificationService;

	@Mock
	private MemberService memberService;

	@Mock
	private RoomRepository roomRepository;

	@Mock
	private RoutineRepository routineRepository;

	@Mock
	private ParticipantRepository participantRepository;

	@Mock
	private CertificationRepository certificationRepository;

	@Mock
	private CertificationsSearchRepository certificationsSearchRepository;

	@Mock
	private ParticipantSearchRepository participantSearchRepository;

	@Mock
	private DailyRoomCertificationRepository dailyRoomCertificationRepository;

	@Mock
	private DailyMemberCertificationRepository dailyMemberCertificationRepository;

	@Mock
	private ImageService imageService;

	@Mock
	private ClockHolder clockHolder;

	@Spy
	private Room room;

	@Spy
	private Participant participant;

	private Member member1;
	private Member member2;
	private Member member3;
	private LocalDate today;
	private Long memberId;
	private Long roomId;

	@BeforeEach
	void init() {
		room = spy(RoomFixture.room());
		participant = spy(RoomFixture.participant(room, 1L));
		member1 = MemberFixture.member("1", "회원1");
		member2 = MemberFixture.member("2", "회원2");
		member3 = MemberFixture.member("3", "회원3");

		lenient().when(room.getId()).thenReturn(1L);
		lenient().when(participant.getRoom()).thenReturn(room);

		today = LocalDate.now();
		memberId = 1L;
		roomId = room.getId();
		room.levelUp();
		room.levelUp();
	}

	@DisplayName("방 인증 전 개인 인증 후 정보 불러오기 성공")
	@Test
	void get_certified_member_info_success() {
		// given
		List<Routine> routines = RoomFixture.routines(room);
		DailyMemberCertification dailyMemberCertification =
			RoomFixture.dailyMemberCertification(memberId, roomId, participant);
		List<String> imageUrls = new ArrayList<>();
		imageUrls.add("https://image.moabam.com/certifications/20231108/1_asdfsdfxcv-4815vcx-asfd");
		imageUrls.add("https://image.moabam.com/certifications/20231108/2_asdfsdfxcv-4815vcx-asfd");

		given(clockHolder.times()).willReturn(LocalDateTime.now().withHour(9).withMinute(58));
		given(clockHolder.date()).willReturn(today);
		given(participantSearchRepository.findOne(memberId, roomId)).willReturn(Optional.of(participant));
		given(memberService.findMember(memberId)).willReturn(member1);
		given(routineRepository.findById(1L)).willReturn(Optional.of(routines.get(0)));
		given(routineRepository.findById(2L)).willReturn(Optional.of(routines.get(1)));
		given(dailyMemberCertificationRepository.save(any(DailyMemberCertification.class))).willReturn(
			dailyMemberCertification);
		given(certificationRepository.saveAll(anyList())).willReturn(List.of());

		// when
		CertifiedMemberInfo certifiedMemberInfo = certificationService.getCertifiedMemberInfo(memberId, roomId,
			imageUrls);

		// then
		assertThat(certifiedMemberInfo.member().getNickname()).isEqualTo(member1.getNickname());
		assertThat(certifiedMemberInfo.bugType()).isEqualTo(BugType.MORNING);
		assertThat(certifiedMemberInfo.date()).isEqualTo(today);
	}

	@DisplayName("이미 인증되어 있는 방에서 루틴 인증 성공")
	@Test
	void already_certified_room_routine_success() {
		// given
		DailyRoomCertification dailyRoomCertification = RoomFixture.dailyRoomCertification(roomId, today);

		given(clockHolder.date()).willReturn(today);
		given(certificationsSearchRepository.findDailyRoomCertification(roomId, today)).willReturn(
			Optional.of(dailyRoomCertification));

		CertifiedMemberInfo certifyInfo = CertificationsMapper.toCertifiedMemberInfo(clockHolder.date(),
			BugType.MORNING, room,
			member1);

		// when
		certificationService.certifyRoom(certifyInfo);

		// then
		assertThat(member1.getBug().getMorningBug()).isEqualTo(12);
	}

	@DisplayName("인증되지 않은 방에서 루틴 인증 후 방의 인증 성공")
	@Test
	void not_certified_room_routine_success() {
		// given
		List<DailyMemberCertification> dailyMemberCertifications =
			RoomFixture.dailyMemberCertifications(roomId, participant);

		given(clockHolder.date()).willReturn(today);
		given(certificationsSearchRepository.findSortedDailyMemberCertifications(roomId, today))
			.willReturn(dailyMemberCertifications);
		given(memberService.getRoomMembers(anyList())).willReturn(List.of(member1, member2, member3));

		CertifiedMemberInfo certifyInfo = CertificationsMapper.toCertifiedMemberInfo(clockHolder.date(),
			BugType.MORNING, room,
			member1);

		// when
		certificationService.certifyRoom(certifyInfo);

		// then
		assertThat(member1.getBug().getMorningBug()).isEqualTo(12);
		assertThat(member2.getBug().getMorningBug()).isEqualTo(12);
		assertThat(member3.getBug().getMorningBug()).isEqualTo(12);
		assertThat(member3.getBug().getNightBug()).isEqualTo(20);
		assertThat(member3.getBug().getGoldenBug()).isEqualTo(30);
		assertThat(room.getExp()).isEqualTo(1);
		assertThat(room.getLevel()).isEqualTo(2);
	}
}
