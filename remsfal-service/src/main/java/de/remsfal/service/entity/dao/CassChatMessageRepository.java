package de.remsfal.service.entity.dao;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import com.datastax.oss.driver.api.querybuilder.delete.Delete;
import com.datastax.oss.driver.api.querybuilder.insert.Insert;
import com.datastax.oss.driver.api.querybuilder.select.Select;
import com.datastax.oss.driver.api.querybuilder.update.Update;
import de.remsfal.service.entity.dto.CassChatMessageEntity;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class CassChatMessageRepository {

    private static final String KEYSPACE = "remsfal";
    private static final String ACTIVE_TABLE = "active_chat_messages";
    private static final String ARCHIVED_TABLE = "archived_chat_messages";

    @Inject
    CqlSession cqlSession;

    public void saveToActive(CassChatMessageEntity message) {
        saveMessage(ACTIVE_TABLE, message);
    }

    public void saveToArchived(CassChatMessageEntity message) {
        saveMessage(ARCHIVED_TABLE, message);
    }

    private void saveMessage(String tableName, CassChatMessageEntity message) {
        Insert insertQuery = QueryBuilder.insertInto(KEYSPACE, tableName)
                .value("chat_session_id", QueryBuilder.literal(message.getChatSessionId()))
                .value("message_id", QueryBuilder.literal(message.getMessageId()))
                .value("sender_id", QueryBuilder.literal(message.getSenderId()))
                .value("content_type", QueryBuilder.literal(message.getContentType()))
                .value("content", QueryBuilder.literal(message.getContent()))
                .value("url", QueryBuilder.literal(message.getUrl()))
                .value("created_at", QueryBuilder.literal(message.getCreatedAt()));

        cqlSession.execute(insertQuery.build());
    }

    public Optional<CassChatMessageEntity> findActiveById(UUID chatSessionId, UUID messageId) {
        return findMessageById(ACTIVE_TABLE, chatSessionId, messageId);
    }

    public Optional<CassChatMessageEntity> findArchivedById(UUID chatSessionId, UUID messageId) {
        return findMessageById(ARCHIVED_TABLE, chatSessionId, messageId);
    }

    private Optional<CassChatMessageEntity> findMessageById(String tableName, UUID chatSessionId, UUID messageId) {
        Select selectQuery = QueryBuilder.selectFrom(KEYSPACE, tableName)
                .all()
                .whereColumn("chat_session_id").isEqualTo(QueryBuilder.literal(chatSessionId))
                .whereColumn("message_id").isEqualTo(QueryBuilder.literal(messageId));

        ResultSet resultSet = cqlSession.execute(selectQuery.build());
        return resultSet.all().stream().map(CassChatMessageEntity::mapRow).findFirst();
    }

    public List<CassChatMessageEntity> findAllActive() {
        return findAllMessages(ACTIVE_TABLE);
    }

    public List<CassChatMessageEntity> findAllArchived() {
        return findAllMessages(ARCHIVED_TABLE);
    }

    private List<CassChatMessageEntity> findAllMessages(String tableName) {
        Select selectQuery = QueryBuilder.selectFrom(KEYSPACE, tableName).all();
        ResultSet resultSet = cqlSession.execute(selectQuery.build());
        return resultSet.all().stream().map(CassChatMessageEntity::mapRow).toList();
    }

    public void updateActive(CassChatMessageEntity message) {
        updateMessage(ACTIVE_TABLE, message);
    }

    public void updateArchived(CassChatMessageEntity message) {
        updateMessage(ARCHIVED_TABLE, message);
    }

    private void updateMessage(String tableName, CassChatMessageEntity message) {
        Update updateQuery = QueryBuilder.update(KEYSPACE, tableName)
                .setColumn("content", QueryBuilder.literal(message.getContent()))
                .setColumn("url", QueryBuilder.literal(message.getUrl()))
                .whereColumn("chat_session_id").isEqualTo(QueryBuilder.literal(message.getChatSessionId()))
                .whereColumn("message_id").isEqualTo(QueryBuilder.literal(message.getMessageId()));

        cqlSession.execute(updateQuery.build());
    }

    public void deleteActive(UUID chatSessionId, UUID messageId) {
        deleteMessage(ACTIVE_TABLE, chatSessionId, messageId);
    }

    public void deleteArchived(UUID chatSessionId, UUID messageId) {
        deleteMessage(ARCHIVED_TABLE, chatSessionId, messageId);
    }

    private void deleteMessage(String tableName, UUID chatSessionId, UUID messageId) {
        Delete deleteQuery = QueryBuilder.deleteFrom(KEYSPACE, tableName)
                .whereColumn("chat_session_id").isEqualTo(QueryBuilder.literal(chatSessionId))
                .whereColumn("message_id").isEqualTo(QueryBuilder.literal(messageId));

        cqlSession.execute(deleteQuery.build());
    }
}
