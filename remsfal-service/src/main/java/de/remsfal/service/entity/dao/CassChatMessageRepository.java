package de.remsfal.service.entity.dao;

import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import de.remsfal.service.cassandra.CassandraService;
import de.remsfal.service.entity.dto.CassChatMessageEntity;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class CassChatMessageRepository {

    private static final String KEYSPACE = "remsfal";
    private static final String TABLE = "chat_messages";

    @Inject
    CassandraService cassandraService;

    public void save(CassChatMessageEntity message) {
        var insertQuery = QueryBuilder.insertInto(KEYSPACE, TABLE)
                .value("chatSessionId", QueryBuilder.literal(message.getChatSessionId()))
                .value("messageId", QueryBuilder.literal(message.getMessageId()))
                .value("senderId", QueryBuilder.literal(message.getSenderId()))
                .value("contentType", QueryBuilder.literal(message.getContentType()))
                .value("content", QueryBuilder.literal(message.getContent()))
                .value("url", QueryBuilder.literal(message.getUrl()))
                .value("createdAt", QueryBuilder.literal(message.getCreatedAt()));

        cassandraService.save(KEYSPACE, TABLE, message, insertQuery);
    }

    public Optional<CassChatMessageEntity> findById(UUID messageId) {
        var selectQuery = QueryBuilder.selectFrom(KEYSPACE, TABLE)
                .all()
                .whereColumn("messageId").isEqualTo(QueryBuilder.literal(messageId));
        return cassandraService.findById(KEYSPACE, TABLE, messageId, CassChatMessageEntity.class, selectQuery);
    }

    public List<CassChatMessageEntity> findAll() {
        var selectQuery = QueryBuilder.selectFrom(KEYSPACE, TABLE).all();
        return cassandraService.findAll(KEYSPACE, TABLE, CassChatMessageEntity.class, selectQuery);
    }

    public void update(CassChatMessageEntity message) {
        var updateQuery = QueryBuilder.update(KEYSPACE, TABLE)
                .setColumn("content", QueryBuilder.literal(message.getContent()))
                .setColumn("url", QueryBuilder.literal(message.getUrl()))
                .whereColumn("messageId").isEqualTo(QueryBuilder.literal(message.getMessageId()));
        cassandraService.update(KEYSPACE, TABLE, updateQuery);
    }

    public void delete(UUID messageId) {
        cassandraService.delete(KEYSPACE, TABLE, messageId);
    }
}
