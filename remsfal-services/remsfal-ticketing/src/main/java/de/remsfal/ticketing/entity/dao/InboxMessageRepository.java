package de.remsfal.ticketing.entity.dao;

import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import com.datastax.oss.driver.api.querybuilder.update.Update;
import com.datastax.oss.driver.api.querybuilder.insert.Insert;
import com.datastax.oss.driver.api.querybuilder.delete.Delete;

import de.remsfal.ticketing.entity.dto.InboxMessageEntity;
import de.remsfal.ticketing.entity.dto.InboxMessageKey;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class InboxMessageRepository extends AbstractRepository<InboxMessageEntity, InboxMessageKey> {

    @ConfigProperty(name = "quarkus.cassandra.keyspace")
    String keyspace;

    private static final String TABLE = "inbox_messages";

    private static final String USER_ID = "user_id";
    private static final String MESSAGE_ID = "id";

    public List<InboxMessageEntity> findByUserId(String userId) {
        return template.select(InboxMessageEntity.class)
            .where(USER_ID).eq(userId)
            .result();
    }

    public List<InboxMessageEntity> findByUserIdAndRead(String userId, Boolean read) {
        return template.select(InboxMessageEntity.class)
            .where(USER_ID).eq(userId)
            .and("read").eq(read)
            .result();
    }

    public List<InboxMessageEntity> findByUserIdAndEventType(String userId, String eventType) {
        return template.select(InboxMessageEntity.class)
            .where(USER_ID).eq(userId)
            .and("event_type").eq(eventType)
            .result();
    }

    public List<InboxMessageEntity> findByUserIdAndEventTypeAndRead(String userId, String eventType, Boolean read) {
        return template.select(InboxMessageEntity.class)
            .where(USER_ID).eq(userId)
            .and("event_type").eq(eventType)
            .and("read").eq(read)
            .result();
    }

    public Optional<InboxMessageEntity> findByUserIdAndId(String userId, UUID id) {
        return template.select(InboxMessageEntity.class)
            .where(USER_ID).eq(userId)
            .and(MESSAGE_ID).eq(id)
            .singleResult();
    }

    public void saveInboxMessage(InboxMessageEntity msg) {
        InboxMessageKey key = msg.getKey();

        Insert insert = QueryBuilder.insertInto(keyspace, TABLE)
            .value(USER_ID, QueryBuilder.literal(key.getUserId()))
            .value(MESSAGE_ID, QueryBuilder.literal(key.getId()))
            .value("event_type", QueryBuilder.literal(msg.getEventType()))
            .value("issue_id", QueryBuilder.literal(msg.getIssueId()))
            .value("title", QueryBuilder.literal(msg.getTitle()))
            .value("issue_type", QueryBuilder.literal(msg.getIssueType()))
            .value("status", QueryBuilder.literal(msg.getStatus()))
            .value("description", QueryBuilder.literal(msg.getDescription() == null ? "" : msg.getDescription()))
            .value("actor_email", QueryBuilder.literal(msg.getActorEmail() == null ? "" : msg.getActorEmail()))
            .value("owner_email", QueryBuilder.literal(msg.getOwnerEmail() == null ? "" : msg.getOwnerEmail()))
            .value("link", QueryBuilder.literal(msg.getLink()))
            .value("created_at", QueryBuilder.literal(msg.getCreatedAt()))
            .value("read", QueryBuilder.literal(msg.getRead()));

        cqlSession.execute(insert.build());
    }

    public void updateReadStatus(String userId, UUID messageId, boolean read) {
        Update update = QueryBuilder.update(keyspace, TABLE)
            .setColumn("read", QueryBuilder.literal(read))
            .whereColumn(USER_ID).isEqualTo(QueryBuilder.literal(userId))
            .whereColumn(MESSAGE_ID).isEqualTo(QueryBuilder.literal(messageId));

        cqlSession.execute(update.build());
    }

    public void deleteInboxMessage(String userId, UUID messageId) {
        Delete delete = QueryBuilder.deleteFrom(keyspace, TABLE)
            .whereColumn(USER_ID).isEqualTo(QueryBuilder.literal(userId))
            .whereColumn(MESSAGE_ID).isEqualTo(QueryBuilder.literal(messageId));

        cqlSession.execute(delete.build());
    }
}
