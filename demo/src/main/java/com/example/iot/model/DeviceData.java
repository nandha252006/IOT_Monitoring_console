package com.example.iot.model;

/**
 * DeviceData is a Data Transfer Object (DTO) that represents the telemetry payload
 * published by simulated IoT devices.
 *
 * Example JSON:
 * {
 *   "deviceId": "sensor1",
 *   "temperature": 35.0,
 *   "battery": 70
 * }
 *
 * This class is designed to be deserialized from JSON by Jackson ObjectMapper.
 */
public class DeviceData {

    // Unique identifier of the virtual IoT device (e.g., "sensor1")
    private String deviceId;

    // Simulated temperature reading in Celsius (typically 20 to 40)
    private double temperature;

    // Simulated battery level percentage (0 to 100)
    private int battery;

    /**
     * Default constructor.
     * Jackson ObjectMapper requires a no-argument constructor to instantiate
     * the object before dynamically invoking setter methods during JSON deserialization.
     */
    public DeviceData() {
    }

    /**
     * Parameterized constructor.
     * Useful for manual creation of DTOs, such as in the simulator or test cases.
     *
     * @param deviceId    Unique identifier of the device
     * @param temperature Current temperature value
     * @param battery     Current battery status level
     */
    public DeviceData(String deviceId, double temperature, int battery) {
        this.deviceId = deviceId;
        this.temperature = temperature;
        this.battery = battery;
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
     * Gets the temperature telemetry reading.
     * @return the temperature
     */
    public double getTemperature() {
        return temperature;
    }

    /**
     * Sets the temperature telemetry reading.
     * @param temperature the temperature to set
     */
    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    /**
     * Gets the battery level percentage.
     * @return the battery level
     */
    public int getBattery() {
        return battery;
    }

    /**
     * Sets the battery level percentage.
     * @param battery the battery level to set
     */
    public void setBattery(int battery) {
        this.battery = battery;
    }

    /**
     * Standard toString implementation to assist with console logging.
     * @return String representation of the telemetry packet
     */
    @Override
    public String toString() {
        return "DeviceData{" +
                "deviceId='" + deviceId + '\'' +
                ", temperature=" + temperature +
                ", battery=" + battery +
                '}';
    }
}
