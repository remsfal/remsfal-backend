package de.remsfal.ticketing.control;

import de.remsfal.core.json.ticketing.InboxMessageJson;
import de.remsfal.ticketing.entity.dto.InboxMessageEntity;
import de.remsfal.ticketing.entity.dto.InboxMessageKey;

import jakarta.enterprise.context.ApplicationScoped;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@ApplicationScoped
public class InboxMessageJsonMapper {

    public InboxMessageJson toJson(InboxMessageEntity entity) {
        if (entity == null) {
            return null;
        }

        InboxMessageJson json = new InboxMessageJson();
        InboxMessageKey key = entity.getKey();

        json.id = key.getId().toString();
        json.userId = key.getUserId();

        json.eventType = entity.getEventType();
        json.issueId = entity.getIssueId();
        json.title = entity.getTitle();
        json.description = entity.getDescription();
        json.issueType = entity.getIssueType();
        json.status = entity.getStatus();
        json.link = entity.getLink();

        json.actorEmail = entity.getActorEmail();
        json.ownerEmail = entity.getOwnerEmail();

        json.read = Boolean.TRUE.equals(entity.getRead());
        json.createdAt = OffsetDateTime.ofInstant(entity.getCreatedAt(), ZoneOffset.UTC);

        return json;
    }

    public List<InboxMessageJson> toJsonList(List<InboxMessageEntity> entities) {
        return entities.stream()
                .map(this::toJson)
                .toList();
    }
}
