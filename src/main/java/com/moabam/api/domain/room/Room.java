package com.moabam.api.domain.room;

import static com.moabam.api.domain.room.RoomType.*;
import static com.moabam.global.error.model.ErrorMessage.*;
import static java.util.Objects.*;

import org.hibernate.annotations.ColumnDefault;

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

	private static final int LEVEL_5 = 5;
	private static final int LEVEL_10 = 10;
	private static final int LEVEL_20 = 20;
	private static final int LEVEL_30 = 30;
	private static final String ROOM_LEVEL_0_IMAGE = "https://image.moabam.com/moabam/default/room-level-00.png";
	private static final String ROOM_LEVEL_5_IMAGE = "https://image.moabam.com/moabam/default/room-level-05.png";
	private static final String ROOM_LEVEL_10_IMAGE = "https://image.moabam.com/moabam/default/room-level-10.png";
	private static final String ROOM_LEVEL_20_IMAGE = "https://image.moabam.com/moabam/default/room-level-20.png";
	private static final String ROOM_LEVEL_30_IMAGE = "https://image.moabam.com/moabam/default/room-level-30.png";
	private static final int MORNING_START_TIME = 4;
	private static final int MORNING_END_TIME = 10;
	private static final int NIGHT_START_TIME = 20;
	private static final int NIGHT_END_TIME = 2;
	private static final int CLOCK_ZERO = 0;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "title", nullable = false, length = 20)
	private String title;

	@Column(name = "password", length = 8)
	private String password;

	@ColumnDefault("0")
	@Column(name = "level", nullable = false)
	private int level;

	@ColumnDefault("0")
	@Column(name = "exp", nullable = false)
	private int exp;

	@Enumerated(value = EnumType.STRING)
	@Column(name = "room_type")
	private RoomType roomType;

	@Column(name = "certify_time", nullable = false)
	private int certifyTime;

	@Column(name = "current_user_count", nullable = false)
	private int currentUserCount;

	@Column(name = "max_user_count", nullable = false)
	private int maxUserCount;

	@Column(name = "announcement", length = 100)
	private String announcement;

	@ColumnDefault("'" + ROOM_LEVEL_0_IMAGE + "'")
	@Column(name = "room_image", length = 500)
	private String roomImage;

	@Column(name = "manager_nickname", length = 30)
	private String managerNickname;

	@Builder
	private Room(Long id, String title, String password, RoomType roomType, int certifyTime, int maxUserCount) {
		this.id = id;
		this.title = requireNonNull(title);
		this.password = password;
		this.level = 0;
		this.exp = 0;
		this.roomType = requireNonNull(roomType);
		this.certifyTime = validateCertifyTime(roomType, certifyTime);
		this.currentUserCount = 1;
		this.maxUserCount = maxUserCount;
		this.roomImage = ROOM_LEVEL_0_IMAGE;
	}

	public void levelUp() {
		this.level += 1;
		this.exp = 0;
		upgradeRoomImage(this.level);
	}

	public void upgradeRoomImage(int level) {
		if (level == LEVEL_5) {
			this.roomImage = ROOM_LEVEL_5_IMAGE;
			return;
		}

		if (level == LEVEL_10) {
			this.roomImage = ROOM_LEVEL_10_IMAGE;
			return;
		}

		if (level == LEVEL_20) {
			this.roomImage = ROOM_LEVEL_20_IMAGE;
			return;
		}

		if (level == LEVEL_30) {
			this.roomImage = ROOM_LEVEL_30_IMAGE;
		}
	}

	public void gainExp() {
		this.exp += 1;
	}

	public void changeAnnouncement(String announcement) {
		this.announcement = announcement;
	}

	public void changeTitle(String title) {
		this.title = title;
	}

	public void changePassword(String password) {
		this.password = password;
	}

	public void changeManagerNickname(String managerNickname) {
		this.managerNickname = managerNickname;
	}

	public void changeMaxCount(int maxUserCount) {
		if (maxUserCount < this.currentUserCount) {
			throw new BadRequestException(ROOM_MAX_USER_COUNT_MODIFY_FAIL);
		}

		this.maxUserCount = maxUserCount;
	}

	public void increaseCurrentUserCount() {
		this.currentUserCount += 1;
	}

	public void decreaseCurrentUserCount() {
		this.currentUserCount -= 1;
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
