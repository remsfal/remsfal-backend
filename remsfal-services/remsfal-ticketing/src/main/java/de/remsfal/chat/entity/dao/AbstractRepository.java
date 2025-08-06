package de.remsfal.chat.entity.dao;

import de.remsfal.chat.entity.dto.AbstractEntity;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.jnosql.mapping.Database;
import org.eclipse.jnosql.mapping.DatabaseType;
import org.eclipse.jnosql.mapping.column.ColumnTemplate;

@ApplicationScoped
public abstract class AbstractRepository<Entity extends AbstractEntity, Key> {

    @Inject
    @Database(value = DatabaseType.COLUMN)
    ColumnTemplate template;

    public static final String SESSION_ID_COLUMN = "session_id";
    public static final String MESSAGE_ID_COLUMN = "message_id";

}