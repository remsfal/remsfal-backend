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

/**
 * Cassandra Database Migration Executor
 * 
 * This class handles Cassandra schema migrations using a custom XML changelog format.
 * It provides functionality similar to Liquibase but tailored for Cassandra-specific requirements.
 * 
 * MIGRATION NOTE: This implementation is maintained alongside Liquibase-ready structures
 * in src/main/resources/db/cassandra/ to facilitate future migration to standard Liquibase.
 * See CASSANDRA-MIGRATION.md for detailed migration strategy.
 * 
 * Features:
 * - Automatic keyspace creation
 * - Ordered script execution based on filename
 * - XML changelog parsing with security features
 * - Startup event-driven migration execution
 * 
 * @author CassandraExecutor (enhanced)
 * @see src/main/resources/db/cassandra/ for Liquibase-ready structure
 */
@ApplicationScoped
public class CassandraExecutor {

    private static final Logger logger = LoggerFactory.getLogger(CassandraExecutor.class);

    private static final String CHANGELOGS_XML_PATH =
        "src/main/resources/cassandra/changelogs/cassandra-changelogs.xml";
    private static final String CQL_SCRIPTS_DIRECTORY =
        "src/main/resources/cassandra/changelogs/cql-scripts";

    @ConfigProperty(name = "quarkus.cassandra.contact-points")
    String cassandraContactPoints;

    @ConfigProperty(name = "quarkus.cassandra.local-datacenter")
    String cassandraLocalDatacenter;

    @ConfigProperty(name = "quarkus.cassandra.keyspace")
    String cassandraKeyspace;

    /**
     * Initializes Cassandra database schema on application startup.
     * 
     * This method ensures the keyspace exists and executes all migration scripts
     * in the correct order based on the changelog XML configuration.
     * 
     * @param event Quarkus startup event
     * @throws RuntimeException if Cassandra initialization fails
     */
    public void onStartup(@Observes StartupEvent event) {
        try (
            CqlSession session = CqlSession.builder()
                .addContactPoint(getContactPoint(cassandraContactPoints))
                .withLocalDatacenter(cassandraLocalDatacenter)
                .build()) {

            logger.info("Initializing Cassandra...");
            ensureKeyspaceExists(session);
            processChangelogs(session);

            logger.info("Cassandra initialization completed.");
        } catch (Exception e) {
            logger.error("Error during Cassandra initialization", e);
            throw new RuntimeException("Failed to initialize Cassandra" +
                    "On " + cassandraContactPoints, e);
        }
    }

    private void ensureKeyspaceExists(CqlSession session) {
        logger.info("Ensuring keyspace '{}' exists.", cassandraKeyspace);
        String createKeyspaceCQL = String.format(
            "CREATE KEYSPACE IF NOT EXISTS %s " +
            "WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 2};",
            cassandraKeyspace
        );
        session.execute(createKeyspaceCQL);
        logger.info("Keyspace '{}' ensured.", cassandraKeyspace);
    }

    private void processChangelogs(CqlSession session) {
        logger.info("Parsing Cassandra changelogs XML in {}", CHANGELOGS_XML_PATH);
        Document changelog = parseChangelogXML();
        NodeList scripts = changelog.getElementsByTagName("script");

        logger.info("Found {} CQL scripts in changelog", scripts.getLength());
        Arrays.stream(getSortedChangelogs(scripts))
                .forEach(scriptPath -> executeCQLScript(session, scriptPath));
    }

    private Document parseChangelogXML() {
        try {
            File xmlFile = new File(CHANGELOGS_XML_PATH);

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            // Disable access to external entities to prevent XXE attacks
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);

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
            logger.info("Executing script: {}", scriptFileName);
            Path scriptPath = Path.of(CQL_SCRIPTS_DIRECTORY, scriptFileName);
            String cqlScript = Files.readString(scriptPath);
            session.execute(cqlScript);
            logger.info("Executed script: {}", scriptFileName);
        } catch (Exception e) {
            logger.error("Failed to execute script: {}", scriptFileName, e);
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

