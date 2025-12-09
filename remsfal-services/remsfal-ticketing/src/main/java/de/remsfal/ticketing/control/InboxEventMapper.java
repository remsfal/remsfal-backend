package de.remsfal.ticketing.control;

import de.remsfal.core.json.ticketing.InboxEventJson;
import de.remsfal.ticketing.entity.dto.InboxMessageEntity;
import de.remsfal.ticketing.entity.dto.InboxMessageKey;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class InboxEventMapper {

    public InboxMessageEntity toEntity(InboxEventJson event) {

        InboxMessageKey key = new InboxMessageKey();
        key.setUserId(event.userId());
        key.setId(UUID.fromString(event.id()));

        InboxMessageEntity entity = new InboxMessageEntity();
        entity.setKey(key);
        entity.setReceivedAt(event.receivedAt().toInstant());
        entity.setType(event.eventType());
        entity.setContractor(event.contractor());
        entity.setSubject(event.subject());
        entity.setProperty(event.property());
        entity.setTenant(event.tenant());
        entity.setRead(false);
        entity.setIssueLink(event.link());

        return entity;
    }
}