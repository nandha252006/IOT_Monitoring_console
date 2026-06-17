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

@Service
public class MonitoringService {

    private static final Logger logger = LoggerFactory.getLogger(MonitoringService.class);
    private final Map<String, DeviceStatus> deviceRegistry = new ConcurrentHashMap<>();

    public void processDeviceData(DeviceData data) {
        String deviceId = data.getDeviceId();
        LocalDateTime now = LocalDateTime.now();

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

        checkAlerts(data);
    }

    private void checkAlerts(DeviceData data) {
        if (data.getTemperature() > 35) {
            logger.warn("!!! ALERT !!! Device [{}] - HIGH TEMPERATURE: {}°C (>35°C)", 
                    data.getDeviceId(), data.getTemperature());
        }

        if (data.getBattery() < 20) {
            logger.warn("!!! ALERT !!! Device [{}] - LOW BATTERY: {}% (<20%)", 
                    data.getDeviceId(), data.getBattery());
        }
    }

    public Collection<DeviceStatus> getAllDevices() {
        return deviceRegistry.values();
    }

    public DeviceStatus getDeviceById(String deviceId) {
        return deviceRegistry.get(deviceId);
    }

    @Scheduled(fixedRate = 5000)
    public void detectOfflineDevices() {
        LocalDateTime thresholdTime = LocalDateTime.now().minus(30, ChronoUnit.SECONDS);

        for (DeviceStatus status : deviceRegistry.values()) {
            if ("ONLINE".equals(status.getStatus()) && status.getLastSeen().isBefore(thresholdTime)) {
                status.setStatus("OFFLINE");
                logger.warn("!!! ALERT !!! Device [{}] went OFFLINE. Last seen at {}", 
                        status.getDeviceId(), status.getLastSeen());
            }
        }
    }
}
