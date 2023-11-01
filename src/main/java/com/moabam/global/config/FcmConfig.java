package com.moabam.global.config;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.moabam.global.error.exception.FcmException;
import com.moabam.global.error.model.ErrorMessage;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableScheduling
public class FcmConfig {

	private static final String FIREBASE_PATH = "config/moabam-firebase.json";

	@Bean
	public FirebaseMessaging firebaseMessaging() {
		try (InputStream inputStream = new ClassPathResource(FIREBASE_PATH).getInputStream()) {
			log.info("======= 1 =======");
			GoogleCredentials credentials = GoogleCredentials.fromStream(inputStream);
			log.info("======= 2 =======");
			FirebaseOptions firebaseOptions = FirebaseOptions.builder()
				.setCredentials(credentials)
				.build();
			log.info("======= 3 =======");
			if (FirebaseApp.getApps().isEmpty()) {
				FirebaseApp.initializeApp(firebaseOptions);
				log.info("======= Firebase init start =======");
			}

			return FirebaseMessaging.getInstance();
		} catch (IOException e) {
			log.error("====== firebase moabam error : " + e);
			throw new FcmException(ErrorMessage.FCM_INIT_FAILED);
		}
	}
}
