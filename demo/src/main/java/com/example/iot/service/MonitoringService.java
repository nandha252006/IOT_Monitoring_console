package com.example.iot.service;

import com.example.iot.model.DeviceData;
import com.example.iot.model.DeviceStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MonitoringService is annotated with Spring's @Service annotation.
 * This registers it as a Service bean in the Spring application context, which holds
 * the core business logic and in-memory state of the device monitoring system.
 *
 * It fulfills:
 * - Stage 6: Process incoming device data
 * - Stage 7: Generate alerts (Console logs for high temp / low battery)
 * - Stage 8: Store latest device status in a HashMap
 * - Stage 9: Maintain lastSeen timestamp
 * - Stage 10: Run offline detection task
 */
@Service
public class MonitoringService {

    // Logger to output monitoring alerts and system events to the console
    private static final Logger logger = LoggerFactory.getLogger(MonitoringService.class);

    // Thread-safe ConcurrentHashMap to store the latest status of each device in-memory
    private final Map<String, DeviceStatus> deviceRegistry = new ConcurrentHashMap<>();

    /**
     * Default constructor.
     * Fulfills coding requirement to document every method.
     */
    public MonitoringService() {
    }

    /**
     * Processes incoming device telemetry data.
     * Updates/inserts the device in the registry, marks it as ONLINE, updates lastSeen,
     * and evaluates temperature/battery alert thresholds.
     *
     * @param data The incoming DTO parsed from the MQTT message payload
     */
    public void processDeviceData(DeviceData data) {
        String deviceId = data.getDeviceId();
        LocalDateTime now = LocalDateTime.now();

        // Stage 8 & 9: Create or update DeviceStatus and mark as ONLINE
        DeviceStatus status = new DeviceStatus(
                deviceId,
                data.getTemperature(),
                data.getBattery(),
                now,
                "ONLINE"
        );

        deviceRegistry.put(deviceId, status);
        logger.info("Updated telemetry for Device [{}]: Temp={}°C, Battery={}%", 
                deviceId, data.getTemperature(), data.getBattery());

        // Stage 7: Check alerts
        checkAlerts(data);
    }

    /**
     * Internal helper to evaluate thresholds and print alerts to the console.
     * - Temperature alert: temperature > 35
     * - Battery alert: battery < 20
     *
     * @param data The device telemetry to evaluate
     */
    private void checkAlerts(DeviceData data) {
        // Temperature Alert
        if (data.getTemperature() > 35) {
            logger.warn("!!! ALERT !!! Device [{}] - HIGH TEMPERATURE detected: {}°C (Threshold: >35.0°C)", 
                    data.getDeviceId(), data.getTemperature());
        }

        // Battery Alert
        if (data.getBattery() < 20) {
            logger.warn("!!! ALERT !!! Device [{}] - LOW BATTERY detected: {}% (Threshold: <20%)", 
                    data.getDeviceId(), data.getBattery());
        }
    }

    /**
     * Retrieves all registered device statuses.
     * Used by the REST controller.
     *
     * @return Collection of all DeviceStatus records
     */
    public Collection<DeviceStatus> getAllDevices() {
        return deviceRegistry.values();
    }

    /**
     * Retrieves status of a specific device by ID.
     * Used by the REST controller.
     *
     * @param deviceId Unique ID of the device
     * @return DeviceStatus if found, null otherwise
     */
    public DeviceStatus getDeviceById(String deviceId) {
        return deviceRegistry.get(deviceId);
    }

    /**
     * Stage 10: Offline detection scheduled task.
     *
     * Spring's @Scheduled annotation schedules this method to run periodically.
     * - fixedRate = 5000 means it executes every 5 seconds (5000 milliseconds).
     *
     * If a device has not sent data in the last 30 seconds (lastSeen timestamp
     * is older than 30 seconds ago) and is still marked ONLINE, we update its status
     * to OFFLINE and print a console alert.
     */
    @Scheduled(fixedRate = 5000)
    public void detectOfflineDevices() {
        LocalDateTime thresholdTime = LocalDateTime.now().minus(30, ChronoUnit.SECONDS);

        for (DeviceStatus status : deviceRegistry.values()) {
            if ("ONLINE".equals(status.getStatus()) && status.getLastSeen().isBefore(thresholdTime)) {
                // Update state in registry to OFFLINE
                status.setStatus("OFFLINE");
                logger.warn("!!! ALERT !!! Device [{}] went OFFLINE. Last seen at {}", 
                        status.getDeviceId(), status.getLastSeen());
            }
        }
    }
}
