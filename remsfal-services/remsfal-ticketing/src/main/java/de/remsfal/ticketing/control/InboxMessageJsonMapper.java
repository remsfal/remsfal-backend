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

        json.receivedAt = OffsetDateTime.ofInstant(entity.getReceivedAt(), ZoneOffset.UTC);
        json.type = entity.getType();
        json.contractor = entity.getContractor();
        json.subject = entity.getSubject();
        json.property = entity.getProperty();
        json.tenant = entity.getTenant();
        json.read = Boolean.TRUE.equals(entity.getRead());
        json.issueLink = entity.getIssueLink();

        return json;
    }

    public List<InboxMessageJson> toJsonList(List<InboxMessageEntity> entities) {
        return entities.stream()
                .map(this::toJson)
                .toList();
    }
}