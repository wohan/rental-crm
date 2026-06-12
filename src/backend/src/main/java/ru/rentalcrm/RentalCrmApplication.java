package ru.rentalcrm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RentalCrmApplication {
    public static void main(String[] args) {
        SpringApplication.run(RentalCrmApplication.class, args);
    }
}
