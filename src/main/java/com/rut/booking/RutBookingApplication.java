package com.rut.booking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class RutBookingApplication {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Moscow"));
        SpringApplication.run(RutBookingApplication.class, args);
    }
}
