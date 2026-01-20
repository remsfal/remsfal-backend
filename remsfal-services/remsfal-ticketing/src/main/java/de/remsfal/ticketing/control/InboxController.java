package de.remsfal.ticketing.control;

import de.remsfal.ticketing.entity.dao.InboxMessageRepository;
import de.remsfal.ticketing.entity.dto.InboxMessageEntity;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class InboxController {

    @Inject
    InboxMessageRepository repository;

    /**
     * Returns inbox messages for a user with optional filtering.
     */
    public List<InboxMessageEntity> getInboxMessages(Boolean read, String userId) {

        if (userId == null) {
            throw new IllegalArgumentException("userId cannot be null");
        }

        List<InboxMessageEntity> result;

        if (read != null) {
            result = repository.findByUserIdAndRead(userId, read);
        }
        else {
            result = repository.findByUserId(userId);
        }

        List<InboxMessageEntity> mutable = new ArrayList<>(result);

        mutable.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));

        return mutable;
    }


    /**
     * Updates read/unread status for a message belonging to a user.
     */
    public InboxMessageEntity updateMessageStatus(String messageId, boolean read, String userId) {
        UUID id;
        try {
            id = UUID.fromString(messageId);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid UUID format: " + messageId);
        }

        Optional<InboxMessageEntity> opt = repository.findByUserIdAndId(userId, id);
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("Inbox message not found for user");
        }

        repository.updateReadStatus(userId, id, read);

        InboxMessageEntity entity = opt.get();
        entity.setRead(read);

        return entity;
    }


    /**
     * Deletes a message from a user's inbox.
     */
    public void deleteMessage(String messageId, String userId) {
        UUID id;
        try {
            id = UUID.fromString(messageId);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid UUID format: " + messageId);
        }

        if (repository.findByUserIdAndId(userId, id).isEmpty()) {
            throw new IllegalArgumentException("Inbox message not found for user");
        }

        repository.deleteInboxMessage(userId, id);
    }
}
