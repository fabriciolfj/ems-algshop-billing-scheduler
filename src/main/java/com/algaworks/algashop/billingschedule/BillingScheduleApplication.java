package com.algaworks.algashop.billingschedule;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BillingScheduleApplication {

	public static void main(String[] args) {
		SpringApplication.run(BillingScheduleApplication.class, args);
	}

}
