package com.pulsedesk.triage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class TriageApplication {

	public static void main(String[] args) {
		SpringApplication.run(TriageApplication.class, args);
	}

}
