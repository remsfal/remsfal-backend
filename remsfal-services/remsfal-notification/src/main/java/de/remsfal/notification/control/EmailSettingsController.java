package de.remsfal.notification.control;

import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.jboss.logging.Logger;

import de.remsfal.core.json.eventing.IssueEventJson.IssueEventType;
import de.remsfal.core.model.notification.EmailSettingsModel;
import de.remsfal.notification.entity.dao.UserEmailSettingsRepository;
import de.remsfal.notification.entity.dto.UserEmailSettingsEntity;

/**
 * Manages which issue emails a user wants to receive. Users without stored settings receive all emails.
 */
@ApplicationScoped
public class EmailSettingsController {

    @Inject
    Logger logger;

    @Inject
    UserEmailSettingsRepository repository;

    @Transactional
    public EmailSettingsModel getSettings(final UUID userId) {
        return repository.findByIdOptional(userId)
            .orElseGet(() -> createDefaults(userId));
    }

    @Transactional
    public EmailSettingsModel updateSettings(final UUID userId, final EmailSettingsModel settings) {
        logger.infov("Updating email settings (userId={0})", userId);
        UserEmailSettingsEntity entity = repository.findById(userId);
        if (entity == null) {
            entity = createDefaults(userId);
            repository.persist(entity);
        }
        if (settings.isIssueCreated() != null) {
            entity.setIssueCreated(settings.isIssueCreated());
        }
        if (settings.isIssueUpdated() != null) {
            entity.setIssueUpdated(settings.isIssueUpdated());
        }
        if (settings.isIssueAssigned() != null) {
            entity.setIssueAssigned(settings.isIssueAssigned());
        }
        if (settings.isIssueMessageCreated() != null) {
            entity.setIssueMessageCreated(settings.isIssueMessageCreated());
        }
        repository.flush();
        return entity;
    }

    /**
     * Checks whether the user wants to receive the email of the given issue event type.
     * Event types without a configurable setting are always enabled.
     */
    @Transactional
    public boolean isEnabled(final UUID userId, final IssueEventType eventType) {
        if (userId == null || eventType == null) {
            return true;
        }
        final EmailSettingsModel settings = getSettings(userId);
        return switch (eventType) {
            case ISSUE_CREATED -> settings.isIssueCreated();
            case ISSUE_UPDATED -> settings.isIssueUpdated();
            case ISSUE_ASSIGNED -> settings.isIssueAssigned();
            case CHAT_MESSAGE_CREATED -> settings.isIssueMessageCreated();
            default -> true;
        };
    }

    @Transactional
    public void deleteSettings(final UUID userId) {
        if (repository.deleteById(userId)) {
            logger.infov("Deleted email settings (userId={0})", userId);
        }
    }

    private static UserEmailSettingsEntity createDefaults(final UUID userId) {
        final UserEmailSettingsEntity entity = new UserEmailSettingsEntity();
        entity.setUserId(userId);
        return entity;
    }

}
