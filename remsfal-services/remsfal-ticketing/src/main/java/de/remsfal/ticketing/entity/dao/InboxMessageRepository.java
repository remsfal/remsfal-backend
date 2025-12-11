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

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class InboxMessageRepository extends AbstractRepository<InboxMessageEntity, InboxMessageKey> {

    @ConfigProperty(name = "quarkus.cassandra.keyspace")
    String keyspace;

    private static final String TABLE = "inbox_messages";

    private static final String USER_ID = "user_id";
    private static final String RECEIVED_AT = "received_at";
    private static final String ID = "id";

    @Inject
    Logger logger;

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

    public List<InboxMessageEntity> findByUserIdAndType(String userId, String type) {
        return template.select(InboxMessageEntity.class)
                .where(USER_ID).eq(userId)
                .and("type").eq(type)
                .result();
    }

    public List<InboxMessageEntity> findByUserIdAndTypeAndRead(String userId, String type, Boolean read) {
        return template.select(InboxMessageEntity.class)
                .where(USER_ID).eq(userId)
                .and("type").eq(type)
                .and("read").eq(read)
                .result();
    }

    public Optional<InboxMessageEntity> findByUserIdAndId(String userId, UUID id) {
        return template.select(InboxMessageEntity.class)
                .where(USER_ID).eq(userId)
                .and(ID).eq(id)
                .singleResult();
    }

    public void saveInboxMessage(InboxMessageEntity message) {
        InboxMessageKey key = message.getKey();

        Insert insert = QueryBuilder.insertInto(keyspace, TABLE)
                .value(USER_ID, QueryBuilder.literal(key.getUserId()))
                .value(ID, QueryBuilder.literal(key.getId()))
                .value(RECEIVED_AT, QueryBuilder.literal(message.getReceivedAt()))
                .value("type", QueryBuilder.literal(message.getType()))
                .value("contractor", QueryBuilder.literal(message.getContractor()))
                .value("subject", QueryBuilder.literal(message.getSubject()))
                .value("property", QueryBuilder.literal(message.getProperty()))
                .value("tenant", QueryBuilder.literal(message.getTenant()))
                .value("read", QueryBuilder.literal(message.getRead()))
                .value("issue_link", QueryBuilder.literal(message.getIssueLink()));

        cqlSession.execute(insert.build());
    }



    public void updateReadStatus(String userId, UUID messageId, boolean read) {

        Update update = QueryBuilder.update(keyspace, TABLE)
                .setColumn("read", QueryBuilder.literal(read))
                .whereColumn(USER_ID).isEqualTo(QueryBuilder.literal(userId))
                .whereColumn(ID).isEqualTo(QueryBuilder.literal(messageId));

        cqlSession.execute(update.build());
    }

    public void deleteInboxMessage(String userId, UUID messageId) {
        Delete delete = QueryBuilder.deleteFrom(keyspace, TABLE)
                .whereColumn(USER_ID).isEqualTo(QueryBuilder.literal(userId))
                .whereColumn(ID).isEqualTo(QueryBuilder.literal(messageId));

        cqlSession.execute(delete.build());
    }
}