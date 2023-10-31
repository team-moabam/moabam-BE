package com.moabam;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@SpringBootApplication
public class MoabamServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(MoabamServerApplication.class, args);
	}
}
