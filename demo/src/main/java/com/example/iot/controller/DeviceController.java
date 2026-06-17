package com.example.iot.controller;

import com.example.iot.model.DeviceStatus;
import com.example.iot.service.MonitoringService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

/**
 * DeviceController is annotated with @RestController, combining @Controller and @ResponseBody.
 * This registers the class as a Spring REST Controller where handler methods directly return
 * JSON representations of data model objects back to HTTP clients.
 *
 * It fulfills:
 * - Stage 11: REST APIs (GET /devices and GET /devices/{deviceId})
 * - Coding Requirements: Constructor injection only
 */
@RestController
public class DeviceController {

    private static final Logger logger = LoggerFactory.getLogger(DeviceController.class);

    // Dependency on the MonitoringService, injected via Constructor Injection
    private final MonitoringService monitoringService;

    /**
     * Parameterized Constructor (Constructor Injection Only).
     * Spring Boot automatically resolves the MonitoringService bean and passes it here.
     *
     * @param monitoringService The service layer managing device statuses
     */
    @Autowired
    public DeviceController(MonitoringService monitoringService) {
        this.monitoringService = monitoringService;
    }

    /**
     * Endpoint to fetch all monitored device statuses.
     * Mapping: GET /devices
     *
     * @return Collection of DeviceStatus objects in JSON format
     */
    @GetMapping("/devices")
    public Collection<DeviceStatus> getAllDevices() {
        logger.info("API request received: GET /devices");
        return monitoringService.getAllDevices();
    }

    /**
     * Endpoint to fetch status for a specific device.
     * Mapping: GET /devices/{deviceId}
     *
     * If the device status exists in the registry, it returns the object with HTTP 200 OK.
     * Otherwise, it returns HTTP 404 Not Found to follow REST API best practices.
     *
     * @param deviceId Unique ID of the device (passed in the path URL)
     * @return ResponseEntity containing either the DeviceStatus or a 404 status
     */
    @GetMapping("/devices/{deviceId}")
    public ResponseEntity<DeviceStatus> getDeviceById(@PathVariable String deviceId) {
        logger.info("API request received: GET /devices/{}", deviceId);
        DeviceStatus status = monitoringService.getDeviceById(deviceId);

        if (status != null) {
            // Return 200 OK containing the device status
            return ResponseEntity.ok(status);
        } else {
            // Return 404 Not Found if device is not in registry
            logger.warn("Device [{}] was not found in registry. Returning 404.", deviceId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
