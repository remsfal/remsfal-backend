package de.remsfal.service.cassandra;

import com.datastax.oss.driver.api.core.CqlSession;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.IntStream;

@ApplicationScoped
public class CassandraExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(CassandraExecutor.class);

    private static final String CHANGELOGS_XML_PATH =
            "src/main/resources/cassandra/changelogs/cassandra-changelogs.xml";
    private static final String CQL_SCRIPTS_DIRECTORY =
            "src/main/resources/cassandra/changelogs/cql-scripts";

    @ConfigProperty(name = "quarkus.cassandra.contact-points")
    String cassandraContactPoints;

    @ConfigProperty(name = "quarkus.cassandra.local-datacenter")
    String cassandraLocalDatacenter;

    private static final String KEYSPACE_NAME = "REMSFAL";

    public void onStartup(@Observes StartupEvent event) {
        try (
                CqlSession session = CqlSession.builder()
                        .addContactPoint(getContactPoint(cassandraContactPoints))
                        .withLocalDatacenter(cassandraLocalDatacenter)
                        .build()) {

            LOGGER.info("Initializing Cassandra...");

            // Ensure keyspace exists
            ensureKeyspaceExists(session);

            // Process changelogs
            processChangelogs(session);

            LOGGER.info("Cassandra initialization completed.");
        } catch (Exception e) {
            LOGGER.error("Error during Cassandra initialization", e);
            throw new RuntimeException("Failed to initialize Cassandra" +
                    "On " + cassandraContactPoints, e);
        }
    }

    private void ensureKeyspaceExists(CqlSession session) {
        LOGGER.info("Ensuring keyspace '{}' exists.", KEYSPACE_NAME);
        String createKeyspaceCQL = String.format(
                "CREATE KEYSPACE IF NOT EXISTS %s " +
                        "WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 2};",
                KEYSPACE_NAME
        );
        session.execute(createKeyspaceCQL);
        LOGGER.info("Keyspace '{}' ensured.", KEYSPACE_NAME);
    }

    private void processChangelogs(CqlSession session) {
        LOGGER.info("Parsing Cassandra changelogs XML in {}", CHANGELOGS_XML_PATH);
        Document changelog = parseChangelogXML();
        NodeList scripts = changelog.getElementsByTagName("script");

        LOGGER.info("Found {} CQL scripts in changelog", scripts.getLength());
        Arrays.stream(getSortedChangelogs(scripts))
                .forEach(scriptPath -> executeCQLScript(session, scriptPath));
    }

    private Document parseChangelogXML() {
        try {
            File xmlFile = new File(CHANGELOGS_XML_PATH);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(xmlFile);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Cassandra changelogs XML", e);
        }
    }

    private String[] getSortedChangelogs(NodeList scripts) {
        return Arrays.stream(IntStream.range(0, scripts.getLength())
                        .mapToObj(scripts::item)
                        .map(scriptNode -> scriptNode.getTextContent().trim())
                        .toArray(String[]::new))
                .sorted(Comparator.naturalOrder())
                .toArray(String[]::new);
    }

    private void executeCQLScript(CqlSession session, String scriptFileName) {
        try {
            LOGGER.info("Executing script: {}", scriptFileName);
            Path scriptPath = Path.of(CQL_SCRIPTS_DIRECTORY, scriptFileName);
            String cqlScript = Files.readString(scriptPath);
            session.execute(cqlScript);
            LOGGER.info("Executed script: {}", scriptFileName);
        } catch (Exception e) {
            LOGGER.error("Failed to execute script: {}", scriptFileName, e);
            throw new RuntimeException("CQL script execution failed", e);
        }
    }

    private InetSocketAddress getContactPoint(String contactPoints) {
        String[] contactPointsArray = contactPoints.split(":");
        if (contactPointsArray.length != 2) {
            throw new IllegalArgumentException("Invalid format. Expected format: host:port");
        }
        return new InetSocketAddress(contactPointsArray[0], Integer.parseInt(contactPointsArray[1]));
    }
}
