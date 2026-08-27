package de.remsfal.ticketing.entity.dao;

import de.remsfal.ticketing.entity.dto.ChatMessageEntity;
import de.remsfal.ticketing.entity.dto.ChatMessageKey;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class ChatMessageRepository extends AbstractRepository<ChatMessageEntity, ChatMessageKey> {

    public ChatMessageEntity insert(final ChatMessageEntity entity) {
        return template.insert(entity);
    }

    public List<ChatMessageEntity> findByIssue(final UUID issueId, final UUID projectId) {
        return template.select(ChatMessageEntity.class)
            .where(ISSUE_ID).eq(issueId)
            .and(PROJECT_ID).eq(projectId)
            .result();
    }
}
