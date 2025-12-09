package de.remsfal.ticketing.control;

import de.remsfal.ticketing.entity.dao.InboxMessageRepository;
import de.remsfal.ticketing.entity.dto.InboxMessageEntity;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class InboxController {

    @Inject
    InboxMessageRepository repository;

    public List<InboxMessageEntity> getInboxMessages(String type, Boolean read, String userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId must not be null");
        }

        List<InboxMessageEntity> result;

        if (type != null && read != null) {
            result = repository.findByUserIdAndTypeAndRead(userId, type, read);
        } else if (type != null) {
            result = repository.findByUserIdAndType(userId, type);
        } else if (read != null) {
            result = repository.findByUserIdAndRead(userId, read);
        } else {
            result = repository.findByUserId(userId);
        }

        List<InboxMessageEntity> sorted = new java.util.ArrayList<>(result);

        sorted.sort((a, b) ->
                b.getReceivedAt().compareTo(a.getReceivedAt())
        );

        return sorted;
    }


    public InboxMessageEntity updateMessageStatus(String messageId, boolean read, String userId) {
        UUID id = UUID.fromString(messageId);

        Optional<InboxMessageEntity> entityOpt = repository.findByUserIdAndId(userId, id);
        if (entityOpt.isEmpty()) {
            throw new IllegalArgumentException("Inbox message not found for user");
        }

        repository.updateReadStatus(userId, id, read);
        return repository.findByUserIdAndId(userId, id).orElseThrow();
    }

    public void deleteMessage(String messageId, String userId) {
        UUID id = UUID.fromString(messageId);

        if (repository.findByUserIdAndId(userId, id).isEmpty()) {
            throw new IllegalArgumentException("Inbox message not found for user");
        }

        repository.deleteInboxMessage(userId, id);
    }
}