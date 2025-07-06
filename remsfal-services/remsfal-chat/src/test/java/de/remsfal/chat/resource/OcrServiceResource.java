package de.remsfal.chat.resource;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import io.quarkus.test.common.DevServicesContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;

import java.time.Duration;
import java.util.List;
import java.util.Map;

public class OcrServiceResource implements QuarkusTestResourceLifecycleManager, DevServicesContext.ContextAware {

    private GenericContainer<?> kafkaContainer;
    private GenericContainer<?> ocrContainer;
    private GenericContainer<?> minioContainer;
    private Network network;

    @Override
    public void setIntegrationTestContext(DevServicesContext context) {
    }

    @Override
    public Map<String, String> start() {
        network = Network.newNetwork();

        kafkaContainer = new GenericContainer<>("apache/kafka:latest")
                .withNetwork(network)
                .withNetworkAliases("kafka")
                .withEnv("KAFKA_NODE_ID", "1")
                .withEnv("KAFKA_PROCESS_ROLES", "broker,controller")
                .withEnv("KAFKA_CONTROLLER_QUORUM_VOTERS", "1@kafka:9093")
                .withEnv("KAFKA_CONTROLLER_LISTENER_NAMES", "CONTROLLER")
                .withEnv("KAFKA_LISTENERS", "DOCKER_INTERNAL://kafka:29092,HOST_EXTERNAL://0.0.0.0:9092,CONTROLLER://kafka:9093")
                .withEnv("KAFKA_ADVERTISED_LISTENERS", "DOCKER_INTERNAL://kafka:29092,HOST_EXTERNAL://localhost:9092")
                .withEnv("KAFKA_LISTENER_SECURITY_PROTOCOL_MAP", "DOCKER_INTERNAL:PLAINTEXT,HOST_EXTERNAL:PLAINTEXT,CONTROLLER:PLAINTEXT")
                .withEnv("KAFKA_INTER_BROKER_LISTENER_NAME", "DOCKER_INTERNAL")
                .withEnv("KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR", "1")
                .withEnv("KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR", "1")
                .withEnv("KAFKA_TRANSACTION_STATE_LOG_MIN_ISR", "1")
                .withEnv("KAFKA_GROUP_INITIAL_REBALANCE_DELAY_MS", "0")
                .withEnv("KAFKA_NUM_PARTITIONS", "1")
                .withExposedPorts(9092);

        ocrContainer = new GenericContainer<>("ghcr.io/remsfal/remsfal-ocr:sha-a1f0971")
                .withNetwork(network)
                .withEnv("KAFKA_BROKER", "kafka:29092")
                .withEnv("MINIO_ENDPOINT", "minio:9000")
                .withEnv("PYTHONUNBUFFERED", "1")
                .waitingFor(Wait.forLogMessage(".*Listening to topic .*\\n", 1))
                .withStartupTimeout(Duration.ofMinutes(2));

        minioContainer = new GenericContainer<>("minio/minio:latest")
                .withNetwork(network)
                .withNetworkAliases("minio")
                .withCommand("server /data")
                .withEnv("MINIO_ROOT_USER", "minioadmin")
                .withEnv("MINIO_ROOT_PASSWORD", "minioadminpassword")
                .withExposedPorts(9000);

        kafkaContainer.setPortBindings(List.of("9092:9092"));
        minioContainer.setPortBindings(List.of("9000:9000"));

        kafkaContainer.start();
        ocrContainer.start();
        minioContainer.start();

        return Map.of();
    }

    @Override
    public void stop() {
        if (ocrContainer != null) ocrContainer.stop();
        if (kafkaContainer != null) kafkaContainer.stop();
        if (network != null) network.close();
    }

}

