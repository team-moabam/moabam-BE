package com.moabam.global.common.constant;

public class FcmConstant {

	public static final String NOTIFICATION_TITLE = "모아밤";
	public static final String KNOCK_BODY = "님이 콕 찔렀습니다.";
	public static final String CERTIFY_TIME_BODY = "방 인증 시간입니다.";
	public static final long EXPIRE_KNOCK = 12;
	public static final long EXPIRE_FCM_TOKEN = 60;
	public static final String CRON_CERTIFY_TIME_EXPRESSION = "0 50 * * * *";
	public static final String KNOCK_KEY = "room_%s_member_%s_knocks_%s";
	public static final String FIREBASE_PATH = "config/moabam-firebase.json";
}
