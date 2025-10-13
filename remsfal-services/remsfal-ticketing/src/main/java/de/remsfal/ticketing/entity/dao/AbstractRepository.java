package de.remsfal.ticketing.entity.dao;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.jnosql.mapping.Database;
import org.eclipse.jnosql.mapping.DatabaseType;
import org.eclipse.jnosql.mapping.column.ColumnTemplate;

import com.datastax.oss.driver.api.core.CqlSession;

import de.remsfal.ticketing.entity.dto.AbstractEntity;

@ApplicationScoped
public abstract class AbstractRepository<Entity extends AbstractEntity, Key> {

    public static final String PROJECT_ID = "project_id";
    public static final String TASK_ID = "task_id";
    public static final String SESSION_ID = "session_id";
    public static final String MESSAGE_ID = "message_id";

    @Inject
    @Database(value = DatabaseType.COLUMN)
    protected ColumnTemplate template;

    @Inject
    protected CqlSession cqlSession;

}