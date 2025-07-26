package de.remsfal.chat.resource;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import io.quarkus.test.common.DevServicesContext;

import org.jboss.logging.Logger;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.IOException;
import java.net.ServerSocket;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class OcrServiceResource implements QuarkusTestResourceLifecycleManager, DevServicesContext.ContextAware {

    public static final String KAFKA_IMAGE = "apache/kafka:latest";
    public static final String MINIO_IMAGE = "minio/minio:latest";
    public static final String RFOCR_IMAGE = "ghcr.io/remsfal/remsfal-ocr:latest";

    private static Logger logger = Logger.getLogger(OcrServiceResource.class);

    private GenericContainer<?> kafkaContainer;
    private GenericContainer<?> ocrContainer;
    private GenericContainer<?> minioContainer;
    private Network network;

    @Override
    public void setIntegrationTestContext(DevServicesContext context) {
        network = Network.newNetwork();
    }

    @Override
    public Map<String, String> start() {
        int kafkaPort = findAvailablePort();
        
        logger.debugv("Creating container for image: {0}", KAFKA_IMAGE);
        kafkaContainer = new GenericContainer<>(KAFKA_IMAGE)
            .withNetwork(network)
            .withNetworkAliases("kafka-broker")
            .withEnv("KAFKA_NODE_ID", "1")
            .withEnv("KAFKA_PROCESS_ROLES", "broker,controller")
            .withEnv("KAFKA_CONTROLLER_QUORUM_VOTERS", "1@kafka-broker:9093")
            .withEnv("KAFKA_CONTROLLER_LISTENER_NAMES", "CONTROLLER")
            .withEnv("KAFKA_LISTENERS", "INTERNAL://0.0.0.0:29092,EXTERNAL://0.0.0.0:"
            + kafkaPort + ",HOST://0.0.0.0:39092,CONTROLLER://0.0.0.0:9093")
            .withEnv("KAFKA_ADVERTISED_LISTENERS", "INTERNAL://kafka-broker:29092,EXTERNAL://localhost:"
            + kafkaPort + ",HOST://localhost:39092")
            .withEnv("KAFKA_LISTENER_SECURITY_PROTOCOL_MAP", "INTERNAL:PLAINTEXT,EXTERNAL:PLAINTEXT,"
            + "HOST:PLAINTEXT,CONTROLLER:PLAINTEXT")
            .withEnv("KAFKA_INTER_BROKER_LISTENER_NAME", "INTERNAL")
            .withEnv("KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR", "1")
            .withEnv("KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR", "1")
            .withEnv("KAFKA_TRANSACTION_STATE_LOG_MIN_ISR", "1")
            .withEnv("KAFKA_GROUP_INITIAL_REBALANCE_DELAY_MS", "0")
            .withEnv("KAFKA_NUM_PARTITIONS", "1")
            .withExposedPorts(39092, kafkaPort);

        logger.debugv("Creating container for image: {0}", MINIO_IMAGE);
        minioContainer = new GenericContainer<>(MINIO_IMAGE)
            .withNetwork(network)
            .withNetworkAliases("minio")
            .withCommand("server /data")
            .withEnv("MINIO_ROOT_USER", "minioadmin")
            .withEnv("MINIO_ROOT_PASSWORD", "minioadminpassword")
            .withExposedPorts(9000);

        logger.debugv("Creating container for image: {0}", RFOCR_IMAGE);
        ocrContainer = new GenericContainer<>(RFOCR_IMAGE)
            .withNetwork(network)
            .withEnv("KAFKA_BROKER", "kafka-broker:29092")
            .dependsOn(kafkaContainer)
            .withEnv("MINIO_ENDPOINT", "minio:9000")
            .dependsOn(minioContainer)
            .withEnv("PYTHONUNBUFFERED", "1")
            .waitingFor(Wait.forLogMessage(".*Listening to topic .*\\n", 1))
            .withStartupTimeout(Duration.ofMinutes(2));

        kafkaContainer.setPortBindings(List.of(kafkaPort + ":" + kafkaPort));

        kafkaContainer.start();
        logger.debugv("Container {0} is starting: {1}", KAFKA_IMAGE, kafkaContainer);
        minioContainer.start();
        logger.debugv("Container {0} is starting: {1}", MINIO_IMAGE, minioContainer);
        ocrContainer.start();
        logger.debugv("Container {0} is starting: {1}", RFOCR_IMAGE, ocrContainer);

        String kafkaBootstrapServers = "localhost:" + kafkaContainer.getMappedPort(kafkaPort);
        Map<String, String> props = new HashMap<>();
        props.put("mp.messaging.connector.smallrye-kafka.bootstrap.servers", kafkaBootstrapServers);
        props.put("quarkus.kafka.bootstrap-servers", kafkaBootstrapServers);
        props.put("quarkus.minio.host", "http://localhost");
        props.put("quarkus.minio.port", String.valueOf(minioContainer.getMappedPort(9000)));
        props.put("quarkus.minio.access-key", "minioadmin");
        props.put("quarkus.minio.secret-key", "minioadminpassword");
        props.put("quarkus.minio.secure", String.valueOf(false));
        return props;
    }

    @Override
    public void stop() {
        if (ocrContainer != null)
            ocrContainer.stop();
        if (kafkaContainer != null)
            kafkaContainer.stop();
        if (network != null)
            network.close();
    }

    public static int findAvailablePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            socket.setReuseAddress(true);
            return socket.getLocalPort();
        } catch (IOException e) {
            throw new IllegalStateException("No available ports", e);
        }
    }

}
