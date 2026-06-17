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

/**
 * DeviceSubscriber is annotated with Spring's @Service annotation.
 * It connects to the MQTT Mosquitto broker and subscribes to telemetry messages.
 *
 * It fulfills:
 * - Stage 2: Subscribe to devices/+
 * - Stage 5: Use Jackson ObjectMapper to deserialize JSON payload to DeviceData
 * - Stage 12: Integrate MQTT subscriber with Spring Boot using @PostConstruct
 * - Coding Requirements: Constructor injection only
 */
@Service
public class DeviceSubscriber implements MqttCallback {

    private static final Logger logger = LoggerFactory.getLogger(DeviceSubscriber.class);

    // MQTT Configuration properties injected via Constructor Injection
    private final String brokerUrl;
    private final String clientId;
    private final String topicPattern;

    // Service dependencies injected via Constructor Injection
    private final MonitoringService monitoringService;
    private final ObjectMapper objectMapper;

    // Paho MQTT Client reference
    private MqttClient mqttClient;

    /**
     * Parameterized Constructor (Constructor Injection Only).
     * Spring Boot automatically wires these dependencies. We use @Value to inject properties from application.properties
     * directly into constructor arguments.
     *
     * @param brokerUrl         The MQTT broker url (e.g. tcp://localhost:1883)
     * @param clientId          The unique MQTT client ID for subscriber
     * @param topicPattern      The MQTT topic pattern (e.g. devices/+)
     * @param monitoringService Service to delegate incoming telemetry processing
     * @param objectMapper      Jackson object mapper to parse JSON payloads
     */
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

    /**
     * Starts the MQTT client and establishes connection.
     *
     * The @PostConstruct annotation tells Spring to execute this method immediately
     * after the dependency injection is completed and the bean is fully constructed.
     *
     * Fulfills Stage 12 requirements.
     */
    @PostConstruct
    public void startSubscription() {
        try {
            logger.info("Initializing MQTT Subscriber client to connect to broker: {}", brokerUrl);

            // Initialize the Paho MqttClient instance
            mqttClient = new MqttClient(brokerUrl, clientId);

            // Configure MQTT connection options
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setAutomaticReconnect(true); // Automatically reconnects if broker goes down
            options.setConnectionTimeout(10);    // Connection timeout in seconds

            // Set this class as the callback handler for messages and connection loss
            mqttClient.setCallback(this);

            // Connect to broker
            mqttClient.connect(options);
            logger.info("MQTT Subscriber successfully connected to broker.");

            // Subscribe to the wildcard topic pattern (Stage 2: devices/+)
            mqttClient.subscribe(topicPattern);
            logger.info("MQTT Subscriber successfully subscribed to pattern: {}", topicPattern);

        } catch (Exception e) {
            logger.error("Failed to start MQTT Subscription client: {}", e.getMessage(), e);
        }
    }

    /**
     * MQTT Callback method triggered when the client loses connection to the broker.
     *
     * @param cause The reason/exception representing the connection loss
     */
    @Override
    public void connectionLost(Throwable cause) {
        logger.warn("MQTT Connection lost! Cause: {}", cause != null ? cause.getMessage() : "Unknown");
    }

    /**
     * MQTT Callback method triggered when a message is published on a subscribed topic.
     * Fulfills Stage 2 & 5.
     *
     * @param topic   The topic name the message arrived on
     * @param message The raw MqttMessage details
     */
    @Override
    public void messageArrived(String topic, MqttMessage message) {
        try {
            // Retrieve string content from message payload bytes
            String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
            logger.debug("Received payload on topic [{}]: {}", topic, payload);

            // Stage 5: Parse raw JSON message payload into DeviceData DTO using Jackson
            DeviceData deviceData = objectMapper.readValue(payload, DeviceData.class);

            // Ensure the deviceId is set. If it's missing from JSON, extract it from topic
            if (deviceData.getDeviceId() == null || deviceData.getDeviceId().isEmpty()) {
                // Topic format: devices/deviceId -> Extract suffix
                String extractedId = topic.substring(topic.lastIndexOf('/') + 1);
                deviceData.setDeviceId(extractedId);
            }

            // Stage 6: Process incoming device data via MonitoringService
            monitoringService.processDeviceData(deviceData);

        } catch (Exception e) {
            logger.error("Error processing incoming message on topic {}: {}", topic, e.getMessage(), e);
        }
    }

    /**
     * MQTT Callback method triggered when a message delivery is completed.
     * (Not utilized since this is a subscriber, but required by MqttCallback interface).
     *
     * @param token The delivery token of the message
     */
    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // No action needed for subscriber
    }

    /**
     * Disconnects the MQTT client when the Spring Context shuts down.
     *
     * The @PreDestroy annotation ensures that resources are clean and connection
     * is terminated gracefully.
     */
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