package com.moabam;

import com.moabam.global.config.OAuthConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@SpringBootApplication
public class MoabamServerApplication {

	@Autowired
	OAuthConfig oAuthConfig;

	public static void main(String[] args) {
		SpringApplication.run(MoabamServerApplication.class, args);
	}
}
