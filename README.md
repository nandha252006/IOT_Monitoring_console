# IOT_Monitoring_console

Here is the complete chronological execution lifecycle of the system, from the very moment you start the applications to the moment they are shut down.

---

### Phase 1: Server Startup (Booting the Monitor)
When you start the Spring Boot application by running `.\mvnw.cmd spring-boot:run`:

1.  **JVM Initialization**: The Java Virtual Machine starts and loads the `IotMonitoringApplication` class.
2.  **Spring Context Bootstrapping**: The `@SpringBootApplication` annotation triggers Spring Boot to:
    *   Start an embedded **Tomcat web server** on port `8080`.
    *   Scan the packages for annotated classes and instantiate them as singleton beans in memory: `MonitoringService`, `DeviceSubscriber`, and `DeviceController`.
    *   Initialize Spring's internal scheduling thread pool (due to `@EnableScheduling`).
3.  **Dependency Injection**: Spring injects the configuration parameters from `application.properties` and wires the dependencies via constructors:
    *   Passes `MonitoringService` and Jackson's `ObjectMapper` to `DeviceSubscriber`.
    *   Passes `MonitoringService` to `DeviceController`.
4.  **MQTT Subscription Hook (`@PostConstruct`)**: 
    *   Right after the `DeviceSubscriber` bean is constructed and injected, Spring automatically triggers `startSubscription()`.
    *   The subscriber establishes a TCP socket connection to the Mosquitto Broker (`tcp://localhost:1883`).
    *   It registers its callback handlers (`messageArrived`, `connectionLost`) and subscribes to the wildcard pattern `devices/+`.
5.  **Offline Detector Daemon Start**: Spring scheduler starts running `detectOfflineDevices()` in a background thread every 5 seconds. Since the registry map is currently empty, it does nothing and exits silently.
6.  **Ready State**: The Spring Boot console logs `Started IotMonitoringApplication...`. The REST API and MQTT Subscriber are now fully active and waiting for data.

---

### Phase 2: Device Simulator Startup (Powering on the Sensors)
When you start the simulation by running the `DevicePublisher` application:

1.  **Main Thread Start**: The JVM starts the standalone simulator.
2.  **Broker Connection**: The simulator instantiates its own `MqttClient` and connects to the Mosquitto broker on `tcp://localhost:1883`.
3.  **Loop Execution**: The simulator enters a continuous `while(true)` loop to simulate the sensors.

---

### Phase 3: Active Operations Loop (Normal Telemetry Flow)
Once both applications are running, they perform the following cyclic loop every 5 seconds:

1.  **Telemetry Generation (Simulator)**:
    *   For `sensor1` and `sensor2`, the simulator generates a random temperature (e.g., $36.2^\circ\text{C}$) and battery level (e.g., $15\%$).
    *   It structures this data as a JSON payload and publishes it to the broker on topic `devices/sensor1`.
2.  **Message Routing (Mosquitto)**:
    *   The Mosquitto broker receives the message from the publisher.
    *   It looks at its subscription routing table, matches `devices/sensor1` to the subscriber's wildcard filter `devices/+`, and forwards the payload to `DeviceSubscriber` over the open TCP socket.
3.  **Ingest & Parsing (Subscriber)**:
    *   The subscriber's `messageArrived()` callback triggers.
    *   Jackson's `ObjectMapper` deserializes the JSON string into a `DeviceData` DTO object.
    *   The subscriber passes the DTO to `MonitoringService.processDeviceData()`.
4.  **State Update & Alerts (Service)**:
    *   The service checks the telemetry. Since battery is $15\%$ ($< 20$) and temperature is $36.2^\circ\text{C}$ ($> 35$), it logs alert warnings to the console.
    *   It updates the `ConcurrentHashMap` registry with a `DeviceStatus` record containing the telemetry, setting the state to `"ONLINE"` and updating the `lastSeen` timestamp to the current system time.
5.  **User Queries (Controller)**:
    *   A user opens a browser and hits `GET http://localhost:8080/devices`.
    *   Tomcat routes the request to `DeviceController.getAllDevices()`.
    *   The controller reads the list from the registry map and returns it.
    *   Spring Boot automatically formats the data into a JSON response which the user sees.

---

### Phase 4: Device Outage (Offline Detection)
What happens if one of the virtual sensors crashes or is turned off?

1.  **Simulator Stopped / Interrupted**: You stop the publisher. No more messages are sent to Mosquitto.
2.  **No Telemetry Ingest**: The subscriber is idle. The `lastSeen` timestamp of the devices in the `MonitoringService` registry stops updating.
3.  **Offline Rule Triggered**:
    *   The background scheduled task runs.
    *   It notices that the current time is 30 seconds past the device's `lastSeen` timestamp.
    *   It updates the status of the device in the map from `"ONLINE"` to `"OFFLINE"`.
    *   It prints a warning alert: `!!! ALERT !!! Device [sensor1] went OFFLINE. Last seen at ...`.
4.  **REST API Reflection**: If the user makes a `GET /devices` API call now, the JSON response will show the device's status as `"OFFLINE"`.

---

### Phase 5: Server Shutdown (Ending the Program)
When you stop the Spring Boot application (e.g., by pressing `Ctrl + C` in the terminal):

1.  **Shutdown Signal**: The JVM receives a termination signal and initiates the Spring context shutdown sequence.
2.  **Graceful Disconnect (`@PreDestroy`)**:
    *   Before destroying the beans, Spring triggers the `@PreDestroy` method `shutdown()` inside `DeviceSubscriber`.
    *   The subscriber cleanly disconnects from the Mosquitto broker and releases socket resources.
3.  **Thread Pool Termination**: The scheduled tasks and background thread pools are stopped.
4.  **Tomcat Stop**: The embedded Tomcat server shuts down, releasing port `8080`.
5.  **Exit**: The JVM exits, and the application execution ends.
