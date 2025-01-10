package de.remsfal.core.model.project;

import java.util.Date;
import java.util.UUID;

/**
 * Represents a chat message in Cassandra.
 */
public interface CassChatMessageModel {

    UUID getChatSessionId(); // Partition key for horizontal scaling

    UUID getMessageId(); // Unique ID for the message (Clustering column)

    UUID getSenderId(); // ID of the sender

    String getContentType(); // Content type (e.g., TEXT, FILE)

    String getContent(); // Text content of the message

    String getUrl(); // File URL if the content type is FILE

    Date getCreatedAt(); // Timestamp when the message was created
}
