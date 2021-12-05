package com.Krip4yk.oril_test_task;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.support.CronExpression;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Timer;

@Configuration
@SpringBootApplication
@EnableJpaRepositories
@EntityScan( basePackages  = { "com.Krip4yk.oril_test_task" })
@EnableScheduling
public class OrilTestTaskApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrilTestTaskApplication.class, args);
	}

}
