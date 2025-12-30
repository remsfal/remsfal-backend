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
        try {
            // Determine environment early
            boolean isCosmosDb = contactPoints.contains("cosmos.azure.com");
            logger.infov("Cassandra contact points: {0}", contactPoints);
            logger.infov("Environment: {0}", isCosmosDb ? "Azure Cosmos DB" : "Local Cassandra");
            
            var builder = CqlSession.builder()
                .addContactPoint(getContactPoint(contactPoints))
                .withLocalDatacenter(localDatacenter);
            
            // Add authentication if credentials are provided
            if (username.isPresent() && password.isPresent()) {
                builder.withAuthCredentials(username.get(), password.get());
            }
            
            // Configure SSL based on environment
            if (isCosmosDb) {
                logger.info("Detected Azure Cosmos DB - enabling SSL");
                javax.net.ssl.SSLContext sslContext;
                try {
                    sslContext = javax.net.ssl.SSLContext.getInstance("TLS");
                    sslContext.init(null, new javax.net.ssl.TrustManager[]{
                        new javax.net.ssl.X509TrustManager() {
                            public java.security.cert.X509Certificate[] getAcceptedIssuers() { return null; }
                            public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) { }
                            public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) { }
                        }
                    }, new java.security.SecureRandom());
                    builder.withSslContext(sslContext);
                } catch (Exception e) {
                    logger.warn("Failed to configure SSL context", e);
                }
            } else {
                logger.info("Detected local Cassandra - explicitly disabling SSL");
                // Explicitly disable SSL for local Cassandra
                builder.withSslContext(null);
            }
            
            // Create keyspace if it doesn't exist (only for local Cassandra)
            if (!isCosmosDb) {
                // Connect without keyspace to create it
                try (CqlSession systemSession = builder.build()) {
                    logger.info("Connected to Cassandra without keyspace...");
                    logger.infov("Ensuring keyspace {0} exists...", keyspace);
                    String createKeyspaceCQL = String.format(
                        "CREATE KEYSPACE IF NOT EXISTS %s " +
                        "WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};",
                        keyspace);
                    systemSession.execute(createKeyspaceCQL);
                    logger.infov("Keyspace {0} ready", keyspace);
                }
                
                // Rebuild the session builder with keyspace for local Cassandra
                builder = CqlSession.builder()
                    .addContactPoint(getContactPoint(contactPoints))
                    .withLocalDatacenter(localDatacenter);
                
                if (username.isPresent() && password.isPresent()) {
                    builder.withAuthCredentials(username.get(), password.get());
                }
                
                builder.withSslContext(null); // No SSL for local Cassandra
            }
            
            // Now connect with keyspace
            builder.withKeyspace(com.datastax.oss.driver.api.core.CqlIdentifier.fromInternal(keyspace));
            
            try (CqlSession session = builder.build()) {
                logger.info("Initializing Cassandra...");
                logger.infov("Connected to keyspace: {0}", keyspace);
                
                if (migrateAtStart) {
                    // Use direct CQL for Cosmos DB (Liquibase JDBC has SSL issues)
                    // Use Liquibase for local Cassandra
                    if (isCosmosDb) {
                        logger.info("Running direct CQL migrations for Azure Cosmos DB");
                        // runDirectMigrations(session); // was created by terraform
                    } else {
                        logger.info("Running Liquibase migrations for local Cassandra");
                        processChangelogs(session);
                    }
                } else {
                    logger.warn("Cassandra migration at start is disabled. Skipping migration.");
                }

                logger.info("Cassandra initialization completed.");
            }
        } catch (Exception e) {
            logger.error("Error during Cassandra initialization", e);
            throw new RuntimeException("Failed to initialize Cassandra at " +
                contactPoints, e);
        }
    }

    private void runDirectMigrations(CqlSession session) {
        logger.info("Running CQL migrations directly (bypassing Liquibase for Cosmos DB)");
        
        // 001-create-issues-table
        logger.info("Creating issues table...");
        session.execute(
            "CREATE TABLE IF NOT EXISTS issues (" +
            "project_id UUID, issue_id UUID, type TEXT, title TEXT, status TEXT, " +
            "reporter_id UUID, tenancy_id UUID, owner_id UUID, description TEXT, " +
            "blocked_by UUID, related_to UUID, duplicate_of UUID, " +
            "created_by UUID, created_at TIMESTAMP, modified_at TIMESTAMP, " +
            "PRIMARY KEY (project_id, issue_id)) " +
            "WITH CLUSTERING ORDER BY (issue_id ASC)");
        // Note: No index on issue_id - it's a clustering key (already indexed)
        // Cosmos DB does not support indexes on clustering keys
        
        // 002-create-chat-sessions-table
        logger.info("Creating chat_sessions table...");
        session.execute(
            "CREATE TABLE IF NOT EXISTS chat_sessions (" +
            "project_id UUID, issue_id UUID, session_id UUID, " +
            "participants MAP<UUID, TEXT>, created_at TIMESTAMP, modified_at TIMESTAMP, " +
            "PRIMARY KEY ((project_id, issue_id), session_id)) " +
            "WITH CLUSTERING ORDER BY (session_id ASC)");
        
        // 003-create-chat-messages-table
        logger.info("Creating chat_messages table...");
        session.execute(
            "CREATE TABLE IF NOT EXISTS chat_messages (" +
            "session_id UUID, message_id UUID, sender_id UUID, " +
            "content_type TEXT, content TEXT, url TEXT, " +
            "created_at TIMESTAMP, modified_at TIMESTAMP, " +
            "PRIMARY KEY (session_id, message_id)) " +
            "WITH CLUSTERING ORDER BY (message_id ASC)");
        
        // 004-add-issues-indexes
        logger.info("Creating indexes on issues table...");
        session.execute("CREATE INDEX IF NOT EXISTS issues_status_idx ON issues (status)");
        session.execute("CREATE INDEX IF NOT EXISTS issues_owner_id_idx ON issues (owner_id)");
        session.execute("CREATE INDEX IF NOT EXISTS issues_tenancy_id_idx ON issues (tenancy_id)");
        
        logger.info("CQL migrations completed successfully");
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
        // set local datacenter (URL-encode to handle spaces)
        try {
            sb.append("&localdatacenter=").append(java.net.URLEncoder.encode(localDatacenter, "UTF-8"));
        } catch (java.io.UnsupportedEncodingException e) {
            sb.append("&localdatacenter=").append(localDatacenter.replace(" ", "%20"));
        }
        
        // SSL configuration for Azure Cosmos DB
        if (contactPoints.contains("cosmos.azure.com")) {
            sb.append("&sslenabled=true");
            sb.append("&sslengineFactory=com.datastax.oss.driver.internal.core.ssl.DefaultSslEngineFactory");
        }
        
        return sb.toString();
    }

    private Properties getLiquibaseProperties() {
        Properties props = new Properties();
        username.ifPresent(user -> props.setProperty("user", user));
        password.ifPresent(pass -> props.setProperty("password", pass));
        
        // SSL properties for Cosmos DB
        if (contactPoints.contains("cosmos.azure.com")) {
            props.setProperty("enablessl", "true");
            props.setProperty("sslengineFactory", "com.datastax.oss.driver.internal.core.ssl.DefaultSslEngineFactory");
        }
        
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