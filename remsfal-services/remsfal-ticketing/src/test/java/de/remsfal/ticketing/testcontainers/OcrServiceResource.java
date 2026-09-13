package de.remsfal.ticketing.testcontainers;

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

public class OcrServiceResource implements QuarkusTestResourceLifecycleManager, DevServicesContext.ContextAware {

    public static final String KAFKA_IMAGE = "apache/kafka:latest";
    public static final String LOCALSTACK_IMAGE = "localstack/localstack:4.9";
    public static final String RFOCR_IMAGE = "ghcr.io/remsfal/remsfal-ocr:latest";

    private static final String LOCALSTACK_ACCESS_KEY = "test";
    private static final String LOCALSTACK_SECRET_KEY = "test";
    private static final String LOCALSTACK_REGION = "us-east-1";

    private static Logger logger = Logger.getLogger(OcrServiceResource.class);

    private GenericContainer<?> kafkaContainer;

    private GenericContainer<?> ocrContainer;

    private GenericContainer<?> localstackContainer;

    private Network network;

    @Override
    public void setIntegrationTestContext(DevServicesContext context) {
        network = Network.newNetwork();
    }

    @Override
    @SuppressWarnings("resource")
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
            .withLabel("quarkus-dev-service-kafka", "OcrServiceResourceKafka")
            .withExposedPorts(39092, kafkaPort);

        logger.debugv("Creating container for image: {0}", LOCALSTACK_IMAGE);
        localstackContainer = new GenericContainer<>(LOCALSTACK_IMAGE)
            .withNetwork(network)
            .withNetworkAliases("localstack")
            .withEnv("SERVICES", "s3")
            .withEnv("DEFAULT_REGION", LOCALSTACK_REGION)
            .withExposedPorts(4566)
            .waitingFor(Wait.forHttp("/_localstack/health").forPort(4566));

        logger.debugv("Creating container for image: {0}", RFOCR_IMAGE);
        ocrContainer = new GenericContainer<>(RFOCR_IMAGE)
            .withNetwork(network)
            .withEnv("KAFKA_BROKER", "kafka-broker:29092")
            .dependsOn(kafkaContainer)
            .withEnv("S3_ENDPOINT", "localstack:4566")
            .withEnv("S3_ACCESS_KEY", LOCALSTACK_ACCESS_KEY)
            .withEnv("S3_SECRET_KEY", LOCALSTACK_SECRET_KEY)
            .dependsOn(localstackContainer)
            .withEnv("PYTHONUNBUFFERED", "1")
            .withLogConsumer(new JBossLogConsumer(logger, "[OCR] "))
            .waitingFor(Wait.forLogMessage(".*Listening to topic.*", 1))
            .withStartupTimeout(Duration.ofMinutes(3));

        kafkaContainer.setPortBindings(List.of(kafkaPort + ":" + kafkaPort));

        kafkaContainer.start();
        logger.debugv("Container {0} is starting: {1}", KAFKA_IMAGE, kafkaContainer);
        localstackContainer.start();
        logger.debugv("Container {0} is starting: {1}", LOCALSTACK_IMAGE, localstackContainer);
        ocrContainer.start();
        logger.debugv("Container {0} is starting: {1}", RFOCR_IMAGE, ocrContainer);

        String kafkaBootstrapServers = "localhost:" + kafkaContainer.getMappedPort(kafkaPort);
        logger.infov("Container {0} is listening on {1}", KAFKA_IMAGE, kafkaBootstrapServers);

        Map<String, String> props = new HashMap<>();
        props.put("mp.messaging.connector.smallrye-kafka.bootstrap.servers", kafkaBootstrapServers);
        props.put("kafka.bootstrap.servers", kafkaBootstrapServers);
        props.put("quarkus.kafka.bootstrap-servers", kafkaBootstrapServers);
        props.put("quarkus.s3.endpoint-override", "http://localhost:" + localstackContainer.getMappedPort(4566));
        props.put("quarkus.s3.aws.region", LOCALSTACK_REGION);
        props.put("quarkus.s3.aws.credentials.type", "static");
        props.put("quarkus.s3.aws.credentials.static-provider.access-key-id", LOCALSTACK_ACCESS_KEY);
        props.put("quarkus.s3.aws.credentials.static-provider.secret-access-key", LOCALSTACK_SECRET_KEY);
        props.put("quarkus.s3.path-style-access", "true");
        return props;
    }

    @Override
    public void stop() {
        if (ocrContainer != null) {
            logger.info("========================================");
            logger.info("OCR Container Final Logs:");
            logger.info("========================================");
            logger.info(ocrContainer.getLogs());
            logger.info("========================================");
            ocrContainer.stop();
        }
        if (kafkaContainer != null)
            kafkaContainer.stop();
        if (localstackContainer != null)
            localstackContainer.stop();
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
