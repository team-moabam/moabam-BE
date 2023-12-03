package com.moabam.support.fixture;

import com.moabam.api.domain.member.BadgeType;
import com.moabam.api.dto.member.MemberInfo;

import java.util.List;

import static com.moabam.global.common.util.BaseImageUrl.*;

public class MemberInfoSearchFixture {

    private static final String NICKNAME = "nickname";
    private static final String PROFILE_IMAGE = IMAGE_DOMAIN + MEMBER_PROFILE_URL;
    private static final String INTRO = "intro";
    private static final long TOTAL_CERTIFY_COUNT = 15;
    private static final String MORNING_EGG = IMAGE_DOMAIN + DEFAULT_MORNING_EGG_URL;
    private static final String NIGHT_EGG = IMAGE_DOMAIN + DEFAULT_NIGHT_EGG_URL;

    public static List<MemberInfo> friendMemberInfo() {
        return friendMemberInfo(TOTAL_CERTIFY_COUNT);
    }

    public static List<MemberInfo> friendMemberInfo(long total) {
        return List.of(
                new MemberInfo(NICKNAME, PROFILE_IMAGE, MORNING_EGG, NIGHT_EGG, INTRO, total, BadgeType.BIRTH,
                        0, 0, 0),
                new MemberInfo(NICKNAME, PROFILE_IMAGE, MORNING_EGG, NIGHT_EGG, INTRO, total, BadgeType.LEVEL10,
                        0, 0, 0)
        );
    }

    public static List<MemberInfo> myInfo(String morningImage, String nightImage) {
        return List.of(
                new MemberInfo(NICKNAME, PROFILE_IMAGE, morningImage, nightImage, INTRO, TOTAL_CERTIFY_COUNT,
                        BadgeType.BIRTH, 0, 0, 0),
                new MemberInfo(NICKNAME, PROFILE_IMAGE, morningImage, nightImage, INTRO, TOTAL_CERTIFY_COUNT,
                        BadgeType.LEVEL10, 0, 0, 0)
        );
    }
}
