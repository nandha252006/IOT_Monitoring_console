package com.example.iot.mqtt;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Random;

public class DevicePublisher {

    private static final Logger logger = LoggerFactory.getLogger(DevicePublisher.class);
    private static final String BROKER_URL = "tcp://localhost:1883";
    private static final String CLIENT_ID = "external-iot-device-simulator";

    public static void main(String[] args) {
        Random random = new Random();

        try {
            logger.info("Initializing IoT Device Simulator. Connecting to broker: {}", BROKER_URL);

            MqttClient client = new MqttClient(BROKER_URL, CLIENT_ID);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setConnectionTimeout(10);

            client.connect(options);
            logger.info("IoT Device Simulator successfully connected to the MQTT broker.");

            String[] deviceIds = {"sensor1", "sensor2"};

            while (true) {
                for (String deviceId : deviceIds) {
                    double temperature = 20.0 + (random.nextDouble() * 20.0);
                    temperature = Math.round(temperature * 10.0) / 10.0;

                    int battery = 10 + random.nextInt(91);

                    String jsonPayload = String.format(
                            "{\"deviceId\":\"%s\",\"temperature\":%.1f,\"battery\":%d}",
                            deviceId, temperature, battery
                    );

                    MqttMessage message = new MqttMessage(jsonPayload.getBytes(StandardCharsets.UTF_8));
                    message.setQos(1);

                    String topic = "devices/" + deviceId;
                    client.publish(topic, message);

                    logger.info("Published to [{}]: {}", topic, jsonPayload);
                }

                Thread.sleep(5000);
            }

        } catch (InterruptedException e) {
            logger.warn("Simulator thread interrupted.");
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logger.error("Simulator encountered error: {}", e.getMessage(), e);
        }
    }
}