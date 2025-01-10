package de.remsfal.service.cassandra;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.mapper.MapperContext;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import com.datastax.oss.driver.api.querybuilder.insert.Insert;
import com.datastax.oss.driver.api.querybuilder.select.Select;
import com.datastax.oss.driver.api.querybuilder.update.Update;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class CassandraService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CassandraService.class);

    private final CqlSession session;

    public CassandraService(CqlSession session) {
        this.session = session;
    }

    public <T> void save(String keyspace, String table, T entity, Insert insertQuery) {
        try {
            session.execute(insertQuery.build());
            LOGGER.info("Saved entity to {}/{}", keyspace, table);
        } catch (Exception e) {
            LOGGER.error("Failed to save entity to {}/{}", keyspace, table, e);
            throw new RuntimeException(e);
        }
    }

    public <T> Optional<T> findById(String keyspace, String table, UUID id, Class<T> entityType, Select selectQuery) {
        try {
            return session.execute(selectQuery.build())
                    .all()
                    .stream()
                    .map(row -> mapRowToEntity(row, entityType))
                    .findFirst();
        } catch (Exception e) {
            LOGGER.error("Failed to find entity in {}/{} by ID {}", keyspace, table, id, e);
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> findAll(String keyspace, String table, Class<T> entityType, Select selectQuery) {
        try {
            return session.execute(selectQuery.build())
                    .all()
                    .stream()
                    .map(row -> mapRowToEntity(row, entityType))
                    .toList();
        } catch (Exception e) {
            LOGGER.error("Failed to find all entities in {}/{}", keyspace, table, e);
            throw new RuntimeException(e);
        }
    }

    public void update(String keyspace, String table, Update updateQuery) {
        try {
            session.execute(updateQuery.build());
            LOGGER.info("Updated entity in {}/{}", keyspace, table);
        } catch (Exception e) {
            LOGGER.error("Failed to update entity in {}/{}", keyspace, table, e);
            throw new RuntimeException(e);
        }
    }

    public void delete(String keyspace, String table, UUID id) {
        try {
            session.execute(QueryBuilder.deleteFrom(keyspace, table).whereColumn("id").isEqualTo(QueryBuilder.literal(id)).build());
            LOGGER.info("Deleted entity from {}/{} by ID {}", keyspace, table, id);
        } catch (Exception e) {
            LOGGER.error("Failed to delete entity from {}/{} by ID {}", keyspace, table, id, e);
            throw new RuntimeException(e);
        }
    }

    private <T> T mapRowToEntity(Object row, Class<T> entityType) {
        // Implement a mapping from Cassandra rows to entities (use reflection, mapper, or a library like Jackson)
        throw new UnsupportedOperationException("Implement mapping logic here.");
    }
}
