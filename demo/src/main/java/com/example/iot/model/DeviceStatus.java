package com.example.iot.model;

import java.time.LocalDateTime;

/**
 * DeviceStatus represents the current state of a registered IoT device
 * inside the monitoring system dashboard.
 *
 * It stores the last received telemetry values, the time it was last seen,
 * and whether the device is currently "ONLINE" or "OFFLINE".
 */
public class DeviceStatus {

    // Unique identifier of the device
    private String deviceId;

    // Last recorded temperature from the device
    private double temperature;

    // Last recorded battery level from the device
    private int battery;

    // Timestamp when the device last published telemetry data (Stage 9)
    private LocalDateTime lastSeen;

    // Connectivity status ("ONLINE" or "OFFLINE") (Stage 8 & 10)
    private String status;

    /**
     * Parameterized constructor.
     * Initializes a device status record.
     *
     * @param deviceId    Unique identifier of the device
     * @param temperature Last recorded temperature
     * @param battery     Last recorded battery level
     * @param lastSeen    Timestamp when data was received
     * @param status      Status of the device (ONLINE / OFFLINE)
     */
    public DeviceStatus(String deviceId, double temperature, int battery, LocalDateTime lastSeen, String status) {
        this.deviceId = deviceId;
        this.temperature = temperature;
        this.battery = battery;
        this.lastSeen = lastSeen;
        this.status = status;
    }

    /**
     * Gets the unique device identifier.
     * @return the device ID
     */
    public String getDeviceId() {
        return deviceId;
    }

    /**
     * Sets the unique device identifier.
     * @param deviceId the device ID to set
     */
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    /**
     * Gets the last known temperature.
     * @return the temperature
     */
    public double getTemperature() {
        return temperature;
    }

    /**
     * Sets the last known temperature.
     * @param temperature the temperature to set
     */
    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    /**
     * Gets the last known battery level.
     * @return the battery level
     */
    public int getBattery() {
        return battery;
    }

    /**
     * Sets the last known battery level.
     * @param battery the battery level to set
     */
    public void setBattery(int battery) {
        this.battery = battery;
    }

    /**
     * Gets the timestamp when telemetry was last received.
     * @return the last seen timestamp
     */
    public LocalDateTime getLastSeen() {
        return lastSeen;
    }

    /**
     * Sets the timestamp when telemetry was last received.
     * @param lastSeen the last seen timestamp to set
     */
    public void setLastSeen(LocalDateTime lastSeen) {
        this.lastSeen = lastSeen;
    }

    /**
     * Gets the connectivity status (ONLINE or OFFLINE).
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the connectivity status (ONLINE or OFFLINE).
     * @param status the status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Helper method to format state for logs.
     * @return String representation of the status state
     */
    @Override
    public String toString() {
        return "DeviceStatus{" +
                "deviceId='" + deviceId + '\'' +
                ", temperature=" + temperature +
                ", battery=" + battery +
                ", lastSeen=" + lastSeen +
                ", status='" + status + '\'' +
                '}';
    }
}
