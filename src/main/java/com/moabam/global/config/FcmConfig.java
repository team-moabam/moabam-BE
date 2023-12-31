package com.moabam.global.config;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.moabam.global.error.exception.FcmException;
import com.moabam.global.error.model.ErrorMessage;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class FcmConfig {

	private static final String FIREBASE_PATH = "config/moabam-firebase.json";

	@Bean
	public FirebaseMessaging firebaseMessaging() {
		try (InputStream inputStream = new ClassPathResource(FIREBASE_PATH).getInputStream()) {
			GoogleCredentials credentials = GoogleCredentials.fromStream(inputStream);
			FirebaseOptions firebaseOptions = FirebaseOptions.builder()
				.setCredentials(credentials)
				.build();

			if (FirebaseApp.getApps().isEmpty()) {
				FirebaseApp.initializeApp(firebaseOptions);
				log.info("======= Firebase init start =======");
			}

			return FirebaseMessaging.getInstance();
		} catch (IOException e) {
			log.error("======= firebase moabam error =======\n" + e);
			throw new FcmException(ErrorMessage.FAILED_FCM_INIT);
		}
	}
}
