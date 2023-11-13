package com.moabam.api.application;

import static com.moabam.api.domain.entity.enums.RoomType.*;
import static org.assertj.core.api.Assertions.*;
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
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.moabam.api.domain.entity.DailyMemberCertification;
import com.moabam.api.domain.entity.DailyRoomCertification;
import com.moabam.api.domain.entity.Member;
import com.moabam.api.domain.entity.Participant;
import com.moabam.api.domain.entity.Room;
import com.moabam.api.domain.entity.Routine;
import com.moabam.api.domain.repository.CertificationRepository;
import com.moabam.api.domain.repository.CertificationsSearchRepository;
import com.moabam.api.domain.repository.DailyMemberCertificationRepository;
import com.moabam.api.domain.repository.DailyRoomCertificationRepository;
import com.moabam.api.domain.repository.ParticipantRepository;
import com.moabam.api.domain.repository.ParticipantSearchRepository;
import com.moabam.api.domain.repository.RoomRepository;
import com.moabam.api.domain.repository.RoutineRepository;
import com.moabam.api.domain.resizedimage.ImageType;
import com.moabam.api.dto.CreateRoomRequest;
import com.moabam.api.dto.RoomMapper;
import com.moabam.global.common.util.ClockHolder;
import com.moabam.support.fixture.MemberFixture;
import com.moabam.support.fixture.RoomFixture;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

	@InjectMocks
	private RoomService roomService;

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
		member1 = MemberFixture.member(1L, "회원1");
		member2 = MemberFixture.member(2L, "회원2");
		member3 = MemberFixture.member(3L, "회원3");

		lenient().when(room.getId()).thenReturn(1L);
		lenient().when(participant.getRoom()).thenReturn(room);

		today = LocalDate.now();
		memberId = 1L;
		roomId = room.getId();
		room.levelUp();
		room.levelUp();
	}

	@DisplayName("비밀번호 없는 방 생성 성공")
	@Test
	void create_room_no_password_success() {
		// given
		List<String> routines = new ArrayList<>();
		routines.add("물 마시기");
		routines.add("코테 풀기");

		CreateRoomRequest createRoomRequest = new CreateRoomRequest(
			"재윤과 앵맹이의 방임", null, routines, MORNING, 10, 4);

		Room expectedRoom = RoomMapper.toRoomEntity(createRoomRequest);
		given(roomRepository.save(any(Room.class))).willReturn(expectedRoom);

		// when
		Long result = roomService.createRoom(1L, createRoomRequest);

		// then
		verify(roomRepository).save(any(Room.class));
		verify(routineRepository).saveAll(ArgumentMatchers.<Routine>anyList());
		verify(participantRepository).save(any(Participant.class));
		assertThat(result).isEqualTo(expectedRoom.getId());
		assertThat(expectedRoom.getPassword()).isNull();
	}

	@DisplayName("비밀번호 있는 방 생성 성공")
	@Test
	void create_room_with_password_success() {
		// given
		List<String> routines = new ArrayList<>();
		routines.add("물 마시기");
		routines.add("코테 풀기");

		CreateRoomRequest createRoomRequest = new CreateRoomRequest(
			"재윤과 앵맹이의 방임", "1234", routines, MORNING, 10, 4);

		Room expectedRoom = RoomMapper.toRoomEntity(createRoomRequest);
		given(roomRepository.save(any(Room.class))).willReturn(expectedRoom);

		// when
		Long result = roomService.createRoom(1L, createRoomRequest);

		// then
		verify(roomRepository).save(any(Room.class));
		verify(routineRepository).saveAll(ArgumentMatchers.<Routine>anyList());
		verify(participantRepository).save(any(Participant.class));
		assertThat(result).isEqualTo(expectedRoom.getId());
		assertThat(expectedRoom.getPassword()).isEqualTo("1234");
	}

	@DisplayName("이미 인증되어 있는 방에서 루틴 인증 성공")
	@Test
	void already_certified_room_routine_success() {
		// given
		List<Routine> routines = RoomFixture.routines(room);
		DailyRoomCertification dailyRoomCertification = RoomFixture.dailyRoomCertification(roomId, today);
		MockMultipartFile image = RoomFixture.makeMultipartFile1();
		List<MultipartFile> images = List.of(image, image, image);
		List<String> uploadImages = new ArrayList<>();
		uploadImages.add("https://image.moabam.com/certifications/20231108/1_asdfsdfxcv-4815vcx-asfd");
		uploadImages.add("https://image.moabam.com/certifications/20231108/2_asdfsdfxcv-4815vcx-asfd");

		given(imageService.uploadImages(images, ImageType.CERTIFICATION)).willReturn(uploadImages);
		given(clockHolder.times()).willReturn(LocalDateTime.now().withHour(9).withMinute(58));
		given(participantSearchRepository.findOne(memberId, roomId)).willReturn(Optional.of(participant));
		given(memberService.getById(memberId)).willReturn(member1);
		given(routineRepository.findById(1L)).willReturn(Optional.of(routines.get(0)));
		given(routineRepository.findById(2L)).willReturn(Optional.of(routines.get(1)));
		given(certificationsSearchRepository.findDailyRoomCertification(roomId, today)).willReturn(
			Optional.of(dailyRoomCertification));

		// when
		roomService.certifyRoom(memberId, roomId, images);

		// then
		assertThat(member1.getBug().getMorningBug()).isEqualTo(12);
		assertThat(member1.getTotalCertifyCount()).isEqualTo(1);
	}

	@DisplayName("인증되지 않은 방에서 루틴 인증 후 방의 인증 성공")
	@Test
	void not_certified_room_routine_success() {
		// given
		List<Routine> routines = RoomFixture.routines(room);
		MockMultipartFile image = RoomFixture.makeMultipartFile1();
		List<DailyMemberCertification> dailyMemberCertifications =
			RoomFixture.dailyMemberCertifications(roomId, participant);
		List<MultipartFile> images = List.of(image, image, image);
		List<String> uploadImages = new ArrayList<>();
		uploadImages.add("https://image.moabam.com/certifications/20231108/1_asdfsdfxcv-4815vcx-asfd");
		uploadImages.add("https://image.moabam.com/certifications/20231108/2_asdfsdfxcv-4815vcx-asfd");

		given(imageService.uploadImages(images, ImageType.CERTIFICATION)).willReturn(uploadImages);
		given(clockHolder.times()).willReturn(LocalDateTime.now().withHour(9).withMinute(58));
		given(participantSearchRepository.findOne(memberId, roomId)).willReturn(Optional.of(participant));
		given(memberService.getById(memberId)).willReturn(member1);
		given(routineRepository.findById(1L)).willReturn(Optional.of(routines.get(0)));
		given(routineRepository.findById(2L)).willReturn(Optional.of(routines.get(1)));
		given(certificationsSearchRepository.findDailyRoomCertification(roomId, today))
			.willReturn(Optional.empty());
		given(certificationsSearchRepository.findSortedDailyMemberCertifications(roomId, today))
			.willReturn(dailyMemberCertifications);
		given(memberService.getRoomMembers(anyList())).willReturn(List.of(member1, member2, member3));

		// when
		roomService.certifyRoom(memberId, roomId, images);

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
