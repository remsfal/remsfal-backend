package de.remsfal.service.cassandra;

import com.datastax.oss.driver.api.core.CqlSession;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
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
public class CassandraInit {

    private static final Logger LOGGER = LoggerFactory.getLogger(CassandraInit.class);

    private static final String CHANGELOGS_XML_PATH = "src/main/resources/cassandra/changelogs/cassandra-changelogs.xml";
    private static final String CQL_SCRIPTS_DIRECTORY = "src/main/resources/cassandra/changelogs/cql-scripts";

    public void onStartup(@Observes StartupEvent event) {
        try (CqlSession session = CqlSession.builder()
                .addContactPoint(new InetSocketAddress("127.0.0.1", 9042)) // Update host/port if necessary
                .withLocalDatacenter("datacenter1") // Match your Cassandra configuration
                .build()) {

            LOGGER.info("Initializing Cassandra schema...");

            // Parse and sort changelog entries
            Document changelog = parseChangelogXML();
            NodeList scripts = changelog.getElementsByTagName("script");

            // Execute each CQL script in order
            Arrays.stream(getSortedChangelogs(scripts))
                    .forEach(scriptPath -> executeCQLScript(session, scriptPath));

            LOGGER.info("Cassandra schema initialization completed.");
        } catch (Exception e) {
            LOGGER.error("Error during Cassandra initialization", e);
            throw new RuntimeException("Failed to initialize Cassandra", e);
        }
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
                .sorted(Comparator.naturalOrder()) // Ensure version order: V1, V2, etc.
                .toArray(String[]::new);
    }

    private void executeCQLScript(CqlSession session, String scriptFileName) {
        try {
            Path scriptPath = Path.of(CQL_SCRIPTS_DIRECTORY, scriptFileName);
            String cqlScript = Files.readString(scriptPath);

            // Include the filename in the CQL as a comment
            String cqlWithComment = "-- Executing script: " + scriptFileName + "\n" + cqlScript;
            session.execute(cqlWithComment);

            LOGGER.info("Executed script: {}", scriptFileName);
        } catch (Exception e) {
            LOGGER.error("Failed to execute script: {}", scriptFileName, e);
            throw new RuntimeException("CQL script execution failed", e);
        }
    }
}
