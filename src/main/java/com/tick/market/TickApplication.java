package com.tick.market;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application bootstrap for the TICK simulated market event ingestion service.
 */
@SpringBootApplication
public class TickApplication {

    public static void main(String[] args) {
        SpringApplication.run(TickApplication.class, args);
    }
}
