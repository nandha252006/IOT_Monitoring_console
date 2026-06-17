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

@RestController
public class DeviceController {

    private static final Logger logger = LoggerFactory.getLogger(DeviceController.class);
    private final MonitoringService monitoringService;

    @Autowired
    public DeviceController(MonitoringService monitoringService) {
        this.monitoringService = monitoringService;
    }

    @GetMapping("/devices")
    public Collection<DeviceStatus> getAllDevices() {
        logger.info("API request received: GET /devices");
        return monitoringService.getAllDevices();
    }

    @GetMapping("/devices/{deviceId}")
    public ResponseEntity<DeviceStatus> getDeviceById(@PathVariable String deviceId) {
        logger.info("API request received: GET /devices/{}", deviceId);
        DeviceStatus status = monitoringService.getDeviceById(deviceId);

        if (status != null) {
            return ResponseEntity.ok(status);
        } else {
            logger.warn("Device [{}] not found. Returning 404.", deviceId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
