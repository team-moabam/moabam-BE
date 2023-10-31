package com.moabam.api.domain.entity;

import static com.moabam.api.domain.entity.enums.RoomType.*;
import static com.moabam.global.error.model.ErrorMessage.*;
import static java.util.Objects.*;

import org.hibernate.annotations.ColumnDefault;

import com.moabam.api.domain.entity.enums.RoomType;
import com.moabam.global.common.entity.BaseTimeEntity;
import com.moabam.global.error.exception.BadRequestException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "room")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Room extends BaseTimeEntity {

	private static final String ROOM_LEVEL_0_IMAGE = "'temptemp'";
	private static final String ROOM_LEVEL_10_IMAGE = "'temp'";
	private static final String ROOM_LEVEL_20_IMAGE = "'tempp'";

	private static final int MORNING_START_TIME = 4;
	private static final int MORNING_END_TIME = 10;
	private static final int NIGHT_START_TIME = 20;
	private static final int NIGHT_END_TIME = 2;
	private static final int CLOCK_ZERO = 0;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	// TODO: 한글 10자도 맞나?
	@Column(name = "title", nullable = false, length = 30)
	private String title;

	@Column(name = "password", length = 8)
	private String password;

	@Column(name = "level", nullable = false)
	private int level;

	@Enumerated(value = EnumType.STRING)
	@Column(name = "type")
	private RoomType roomType;

	@Column(name = "certify_time", nullable = false)
	private int certifyTime;

	@Column(name = "current_user_count", nullable = false)
	private int currentUserCount;

	@Column(name = "max_user_count", nullable = false)
	private int maxUserCount;

	// TODO: 한글 길이 고려
	@Column(name = "announcement", length = 255)
	private String announcement;

	@ColumnDefault(ROOM_LEVEL_0_IMAGE)
	@Column(name = "room_image", length = 500)
	private String roomImage;

	@Builder
	private Room(String title, String password, RoomType roomType, int certifyTime, int maxUserCount) {
		this.title = requireNonNull(title);
		this.password = password;
		this.level = 0;
		this.roomType = requireNonNull(roomType);
		this.certifyTime = validateCertifyTime(roomType, certifyTime);
		this.currentUserCount = 1;
		this.maxUserCount = maxUserCount;
		this.roomImage = ROOM_LEVEL_0_IMAGE;
	}

	public void levelUp() {
		this.level += 1;
	}

	public void changeAnnouncement(String announcement) {
		this.announcement = announcement;
	}

	public void upgradeRoomImage(String roomImage) {
		this.roomImage = roomImage;
	}

	public void changeCertifyTime(int certifyTime) {
		this.certifyTime = validateCertifyTime(this.roomType, certifyTime);
	}

	private int validateCertifyTime(RoomType roomType, int certifyTime) {
		if (roomType.equals(MORNING) && (certifyTime < MORNING_START_TIME || certifyTime > MORNING_END_TIME)) {
			throw new BadRequestException(INVALID_REQUEST_FIELD);
		}

		if (roomType.equals(NIGHT)
			&& ((certifyTime < NIGHT_START_TIME && certifyTime > NIGHT_END_TIME) || certifyTime < CLOCK_ZERO)) {
			throw new BadRequestException(INVALID_REQUEST_FIELD);
		}

		return certifyTime;
	}
}