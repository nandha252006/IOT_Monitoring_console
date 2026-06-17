package com.example.iot.model;

import java.time.LocalDateTime;

public class DeviceStatus {

    private String deviceId;
    private double temperature;
    private int battery;
    private LocalDateTime lastSeen;
    private String status;

    public DeviceStatus(String deviceId, double temperature, int battery, LocalDateTime lastSeen, String status) {
        this.deviceId = deviceId;
        this.temperature = temperature;
        this.battery = battery;
        this.lastSeen = lastSeen;
        this.status = status;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public int getBattery() {
        return battery;
    }

    public void setBattery(int battery) {
        this.battery = battery;
    }

    public LocalDateTime getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(LocalDateTime lastSeen) {
        this.lastSeen = lastSeen;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

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
