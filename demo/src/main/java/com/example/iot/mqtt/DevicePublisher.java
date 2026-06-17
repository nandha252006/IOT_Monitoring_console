package com.example.iot.mqtt;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Random;

/**
 * DevicePublisher is a standalone utility Java class.
 * It does not run inside the Spring Boot container, simulating external physical IoT devices.
 *
 * It fulfills:
 * - Stage 1: Connect to tcp://localhost:1883 and publish to devices/sensor1
 * - Stage 3: Simulate virtual devices generating random battery (10-100) and temp (20-40) every 5 seconds
 * - Stage 4: Format payload in JSON structure
 */
public class DevicePublisher {

    private static final Logger logger = LoggerFactory.getLogger(DevicePublisher.class);

    // Configurable MQTT parameters matching the system environment
    private static final String BROKER_URL = "tcp://localhost:1883";
    private static final String CLIENT_ID = "external-iot-device-simulator";

    /**
     * Standalone main method to start the IoT device simulator.
     * Connects to the local MQTT broker and simulates telemetry publishes in a loop.
     *
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        Random random = new Random();

        try {
            logger.info("Initializing IoT Device Simulator. Connecting to broker: {}", BROKER_URL);

            // Establish MQTT client connection
            MqttClient client = new MqttClient(BROKER_URL, CLIENT_ID);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setConnectionTimeout(10);

            client.connect(options);
            logger.info("IoT Device Simulator successfully connected to the MQTT broker.");

            // List of simulated virtual devices to create a more realistic multi-device environment
            String[] deviceIds = {"sensor1", "sensor2"};

            // Continuous loop to simulate telemetry every 5 seconds (Stage 3)
            while (true) {
                for (String deviceId : deviceIds) {
                    // Stage 3: Generate random values
                    // Temperature: Random double between 20.0 and 40.0
                    double temperature = 20.0 + (random.nextDouble() * 20.0);
                    // Rounding temperature to 1 decimal place for cleaner payloads
                    temperature = Math.round(temperature * 10.0) / 10.0;

                    // Battery: Random integer between 10 and 100
                    int battery = 10 + random.nextInt(91);

                    // Stage 4: Construct JSON payload
                    String jsonPayload = String.format(
                            "{\"deviceId\":\"%s\",\"temperature\":%.1f,\"battery\":%d}",
                            deviceId, temperature, battery
                    );

                    // Create MqttMessage and publish it to the topic: devices/{deviceId}
                    MqttMessage message = new MqttMessage(jsonPayload.getBytes(StandardCharsets.UTF_8));
                    message.setQos(1); // At least once delivery quality of service

                    String topic = "devices/" + deviceId;
                    client.publish(topic, message);

                    logger.info("Published to [{}]: {}", topic, jsonPayload);
                }

                // Sleep for 5 seconds before publishing the next telemetry set (Stage 3)
                Thread.sleep(5000);
            }

        } catch (InterruptedException e) {
            logger.warn("IoT Device Simulator thread was interrupted. Shutting down.");
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logger.error("IoT Device Simulator encountered an error: {}", e.getMessage(), e);
        }
    }
}