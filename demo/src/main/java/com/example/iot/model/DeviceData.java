package com.example.iot.model;

public class DeviceData {

    private String deviceId;
    private double temperature;
    private int battery;

    public DeviceData() {
    }

    public DeviceData(String deviceId, double temperature, int battery) {
        this.deviceId = deviceId;
        this.temperature = temperature;
        this.battery = battery;
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

    @Override
    public String toString() {
        return "DeviceData{" +
                "deviceId='" + deviceId + '\'' +
                ", temperature=" + temperature +
                ", battery=" + battery +
                '}';
    }
}
