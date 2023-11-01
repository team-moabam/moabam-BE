package com.moabam.global.config;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.moabam.global.error.exception.FcmException;
import com.moabam.global.error.model.ErrorMessage;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableScheduling
public class FcmConfig {

	private static final String FIREBASE_PATH = "config/moabam-firebase.json";

	@PostConstruct
	public void initFirebaseMessaging() {
		try (InputStream inputStream = new ClassPathResource(FIREBASE_PATH).getInputStream()) {
			GoogleCredentials credentials = GoogleCredentials.fromStream(inputStream);
			FirebaseOptions firebaseOptions = FirebaseOptions.builder()
				.setCredentials(credentials)
				.build();

			if (FirebaseApp.getApps().isEmpty()) {
				FirebaseApp.initializeApp(firebaseOptions);
				log.info("======= Firebase init start =======");
			}
		} catch (IOException e) {
			throw new FcmException(ErrorMessage.FCM_INIT_FAILED);
		}
	}
}
