package de.remsfal.ticketing.entity;

import com.datastax.oss.driver.api.core.CqlSession;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Properties;

/**
 * Cassandra Database Migration Executor
 *
 * This class handles Cassandra schema migrations using Liquibase.
 */
@ApplicationScoped
public class CassandraExecutor {

    private static final Logger logger = Logger.getLogger(CassandraExecutor.class);

    @ConfigProperty(name = "quarkus.liquibase.migrate-at-start", defaultValue = "false")
    Boolean migrateAtStart;

    @ConfigProperty(name = "quarkus.liquibase.change-log", defaultValue = "db/changeLog.xml")
    String liquibaseChangelog;

    @ConfigProperty(name = "quarkus.cassandra.contact-points")
    String contactPoints;

    @ConfigProperty(name = "quarkus.cassandra.local-datacenter")
    String localDatacenter;

    @ConfigProperty(name = "quarkus.cassandra.keyspace")
    String keyspace;

    @ConfigProperty(name = "quarkus.cassandra.auth.username")
    Optional<String> username;

    @ConfigProperty(name = "quarkus.cassandra.auth.password")
    Optional<String> password;

    /**
     * Initializes Cassandra database schema on application startup.
     *
     * This method ensures the keyspace exists and executes all migration
     * scripts in the correct order based on the changelog XML configuration.
     *
     * @param event Quarkus startup event
     * @throws RuntimeException if Cassandra initialization fails
     */
    public void onStartup(@Observes StartupEvent event) {
        try (
            CqlSession session = CqlSession.builder()
                .addContactPoint(getContactPoint(contactPoints))
                .withLocalDatacenter(localDatacenter)
                .build()) {

            logger.info("Initializing Cassandra...");
            ensureKeyspaceExists(session);
            if (migrateAtStart) {
                processChangelogs(session);
            } else {
                logger.warn("Cassandra migration at start is disabled. Skipping migration.");
            }

            logger.info("Cassandra initialization completed.");
        } catch (Exception e) {
            logger.error("Error during Cassandra initialization", e);
            throw new RuntimeException("Failed to initialize Cassandra at " +
                contactPoints, e);
        }
    }

    private void ensureKeyspaceExists(CqlSession session) {
        logger.infov("Ensuring keyspace {0} exists.", keyspace);
        String createKeyspaceCQL = String.format("CREATE KEYSPACE IF NOT EXISTS %s " +
            "WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};",
            keyspace);
        session.execute(createKeyspaceCQL);
        logger.infov("Keyspace {0} ensured.", keyspace);
    }

    private void processChangelogs(CqlSession session) throws SQLException, LiquibaseException {
        logger.infov("Processing Cassandra changelogs XML at {0}", liquibaseChangelog);

        final String url = buildConnectionURL();
        try (Connection c = DriverManager.getConnection(url, getLiquibaseProperties())) {
            @SuppressWarnings("resource")
            Liquibase lb = new Liquibase(
                liquibaseChangelog,
                new ClassLoaderResourceAccessor(),
                new JdbcConnection(c));
            lb.update((String) null);
        }
    }

    private String buildConnectionURL() {
        StringBuilder sb = new StringBuilder("jdbc:cassandra://");
        // set hosts
        if (contactPoints.split(",").length > 1) {
            sb.append(String.join(",", contactPoints.split(",")));
        } else {
            sb.append(contactPoints);
        }
        // set keyspace
        sb.append("/").append(keyspace);
        // set compliance mode for Liquibase
        sb.append("?compliancemode=Liquibase");
        // set local datacenter
        sb.append("&localdatacenter=").append(localDatacenter);
        return sb.toString();
    }

    private Properties getLiquibaseProperties() {
        Properties props = new Properties();
        username.ifPresent(user -> props.setProperty("user", user));
        password.ifPresent(pass -> props.setProperty("password", pass));
        return props;
    }

    private InetSocketAddress getContactPoint(String contactPoints) {
        String[] contactPointsArray = contactPoints.split(":");
        if (contactPointsArray.length != 2) {
            throw new IllegalArgumentException("Invalid format. Expected format: host:port");
        }
        return new InetSocketAddress(contactPointsArray[0], Integer.parseInt(contactPointsArray[1]));
    }

}