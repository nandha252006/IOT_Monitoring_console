package com.example.iot.mqtt;

import com.example.iot.model.DeviceData;
import com.example.iot.service.MonitoringService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class DeviceSubscriber implements MqttCallback {

    private static final Logger logger = LoggerFactory.getLogger(DeviceSubscriber.class);

    private final String brokerUrl;
    private final String clientId;
    private final String topicPattern;
    private final MonitoringService monitoringService;
    private final ObjectMapper objectMapper;

    private MqttClient mqttClient;

    @Autowired
    public DeviceSubscriber(
            @Value("${mqtt.broker.url}") String brokerUrl,
            @Value("${mqtt.client.subscriber.id}") String clientId,
            @Value("${mqtt.topic.pattern}") String topicPattern,
            MonitoringService monitoringService,
            ObjectMapper objectMapper) {
        this.brokerUrl = brokerUrl;
        this.clientId = clientId;
        this.topicPattern = topicPattern;
        this.monitoringService = monitoringService;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void startSubscription() {
        try {
            logger.info("Initializing MQTT Subscriber client to connect to broker: {}", brokerUrl);
            mqttClient = new MqttClient(brokerUrl, clientId);

            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setAutomaticReconnect(true);
            options.setConnectionTimeout(10);

            mqttClient.setCallback(this);
            mqttClient.connect(options);
            logger.info("MQTT Subscriber successfully connected to broker.");

            mqttClient.subscribe(topicPattern);
            logger.info("MQTT Subscriber successfully subscribed to pattern: {}", topicPattern);

        } catch (Exception e) {
            logger.error("Failed to start MQTT Subscription client: {}", e.getMessage(), e);
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        logger.warn("MQTT Connection lost! Cause: {}", cause != null ? cause.getMessage() : "Unknown");
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        try {
            String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
            logger.debug("Received payload on topic [{}]: {}", topic, payload);

            DeviceData deviceData = objectMapper.readValue(payload, DeviceData.class);

            if (deviceData.getDeviceId() == null || deviceData.getDeviceId().isEmpty()) {
                String extractedId = topic.substring(topic.lastIndexOf('/') + 1);
                deviceData.setDeviceId(extractedId);
            }

            monitoringService.processDeviceData(deviceData);

        } catch (Exception e) {
            logger.error("Error processing incoming message on topic {}: {}", topic, e.getMessage(), e);
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
    }

    @PreDestroy
    public void shutdown() {
        if (mqttClient != null && mqttClient.isConnected()) {
            try {
                mqttClient.disconnect();
                mqttClient.close();
                logger.info("MQTT Subscriber disconnected and closed successfully.");
            } catch (Exception e) {
                logger.error("Error during MQTT Subscriber shutdown: {}", e.getMessage());
            }
        }
    }
}