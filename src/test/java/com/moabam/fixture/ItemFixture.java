package com.moabam.fixture;

import com.moabam.api.domain.entity.Item;
import com.moabam.api.domain.entity.enums.ItemCategory;
import com.moabam.api.domain.entity.enums.RoomType;

public class ItemFixture {

	public static final String MORNING_SANTA_SKIN_NAME = "산타 오목눈이";
	public static final String MORNING_SANTA_SKIN_IMAGE = "/item/morning_santa.png";
	public static final String MORNING_KILLER_SKIN_NAME = "킬러 오목눈이";
	public static final String MORNING_KILLER_SKIN_IMAGE = "/item/morning_killer.png";
	public static final String NIGHT_MAGE_SKIN_NAME = "메이지 부엉이";
	public static final String NIGHT_MAGE_SKIN_IMAGE = "/item/night_mage.png";

	public static Item.ItemBuilder morningSantaSkin() {
		return Item.builder()
			.type(RoomType.MORNING)
			.category(ItemCategory.SKIN)
			.name(MORNING_SANTA_SKIN_NAME)
			.image(MORNING_SANTA_SKIN_IMAGE);
	}

	public static Item.ItemBuilder morningKillerSkin() {
		return Item.builder()
			.type(RoomType.MORNING)
			.category(ItemCategory.SKIN)
			.name(MORNING_KILLER_SKIN_NAME)
			.image(MORNING_KILLER_SKIN_IMAGE);
	}

	public static Item nightMageSkin() {
		return Item.builder()
			.type(RoomType.NIGHT)
			.category(ItemCategory.SKIN)
			.name(NIGHT_MAGE_SKIN_NAME)
			.image(NIGHT_MAGE_SKIN_IMAGE)
			.build();
	}
}
