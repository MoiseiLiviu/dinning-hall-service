package com.restaurantapp.dinninghallservice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@Slf4j
public class DinningHallServiceApplication {

	public static final Long TIME_UNIT = 50L;

	public static void main(String[] args) {
		log.info("Starting the app");
		SpringApplication.run(DinningHallServiceApplication.class, args);
	}
}
