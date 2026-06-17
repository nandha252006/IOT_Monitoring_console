package com.example.iot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * IotMonitoringApplication is the main entry point for the Spring Boot application.
 *
 * Annotations used:
 * - @SpringBootApplication: A convenience annotation that combines:
 *   1. @Configuration: Tags the class as a source of bean definitions.
 *   2. @EnableAutoConfiguration: Tells Spring Boot to start adding beans based on classpath settings.
 *   3. @ComponentScan: Scans the com.example.iot package for components (@Service, @RestController, etc.).
 *
 * - @EnableScheduling: Enables Spring's scheduled task execution capability.
 *   This is required for the @Scheduled offline detector method in MonitoringService to run periodically.
 */
@SpringBootApplication
@EnableScheduling
public class IotMonitoringApplication {

    /**
     * Standard main method that launches the Spring Boot application context.
     *
     * @param args Command line arguments passed during startup
     */
    public static void main(String[] args) {
        SpringApplication.run(IotMonitoringApplication.class, args);
    }
}
