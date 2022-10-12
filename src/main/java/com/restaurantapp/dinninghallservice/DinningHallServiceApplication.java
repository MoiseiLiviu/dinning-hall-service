package com.restaurantapp.dinninghallservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DinningHallServiceApplication {

	public static final Long TIME_UNIT = 50L;

	public static void main(String[] args) {
		SpringApplication.run(DinningHallServiceApplication.class, args);
	}
}
