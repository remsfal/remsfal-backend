package de.remsfal.service.cassandra;

import com.datastax.oss.driver.api.core.CqlSession;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class CassandraService{
    private static final Logger LOGGER = LoggerFactory.getLogger(CassandraService.class);

    @Inject
    CqlSession cqlSession;
}
