package com.moabam.support.fixture;

import static com.moabam.global.common.util.BaseImageUrl.*;

import com.moabam.api.domain.item.Item;
import com.moabam.api.domain.item.ItemCategory;
import com.moabam.api.domain.item.ItemType;

public class ItemFixture {

	public static final String MORNING_SANTA_SKIN_NAME = "산타 오목눈이";
	public static final String MORNING_SANTA_SKIN_IMAGE = IMAGE_DOMAIN + "item/morning_santa.png";
	public static final String MORNING_KILLER_SKIN_NAME = "킬러 오목눈이";
	public static final String MORNING_KILLER_SKIN_IMAGE = IMAGE_DOMAIN + "item/morning_killer.png";
	public static final String NIGHT_MAGE_SKIN_NAME = "메이지 부엉이";
	public static final String NIGHT_MAGE_SKIN_IMAGE = IMAGE_DOMAIN + "item/night_mage.png";

	public static Item.ItemBuilder morningSantaSkin() {
		return Item.builder()
			.type(ItemType.MORNING)
			.category(ItemCategory.SKIN)
			.name(MORNING_SANTA_SKIN_NAME)
			.image(MORNING_SANTA_SKIN_IMAGE);
	}

	public static Item.ItemBuilder morningKillerSkin() {
		return Item.builder()
			.type(ItemType.MORNING)
			.category(ItemCategory.SKIN)
			.name(MORNING_KILLER_SKIN_NAME)
			.image(MORNING_KILLER_SKIN_IMAGE);
	}

	public static Item nightMageSkin() {
		return Item.builder()
			.type(ItemType.NIGHT)
			.category(ItemCategory.SKIN)
			.name(NIGHT_MAGE_SKIN_NAME)
			.image(NIGHT_MAGE_SKIN_IMAGE)
			.build();
	}
}
