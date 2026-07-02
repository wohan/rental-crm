package ru.rentcrm.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class RentCrmApplication {
    public static void main(String[] args) {
        SpringApplication.run(RentCrmApplication.class, args);
    }
}
