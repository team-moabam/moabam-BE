package com.moabam.api.domain.entity;

import static java.util.Objects.*;

import org.hibernate.annotations.ColumnDefault;

import com.moabam.api.domain.entity.enums.RoomType;
import com.moabam.global.common.entity.BaseTimeEntity;

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
@Table(name = "rooms")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Room extends BaseTimeEntity {

	// TODO: 방 레벨별 이미지
	private final String ROOM_LEVEL_0_IMAGE = "";
	private final String ROOM_LEVEL_10_IMAGE = "";
	private final String ROOM_LEVEL_20_IMAGE = "";

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
	private Room(String title, String password, RoomType roomType, int certifyTime, int maxUserCount,
		String announcement) {
		this.title = requireNonNull(title);
		this.password = password;
		this.level = 0;
		this.roomType = requireNonNull(roomType);
		this.certifyTime = certifyTime;
		this.currentUserCount = 1;
		this.maxUserCount = maxUserCount;
		this.announcement = announcement;
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
}
