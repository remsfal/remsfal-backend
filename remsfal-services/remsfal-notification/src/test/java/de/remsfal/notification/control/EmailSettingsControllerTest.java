package de.remsfal.notification.control;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;

import de.remsfal.core.json.eventing.IssueEventJson.IssueEventType;
import de.remsfal.core.json.notification.ImmutableEmailSettingsJson;
import de.remsfal.core.model.notification.EmailSettingsModel;
import de.remsfal.test.AbstractTest;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class EmailSettingsControllerTest extends AbstractTest {

    @Inject
    EmailSettingsController controller;

    @Test
    void getSettings_SUCCESS_defaultsWithoutStoredSettings() {
        final UUID userId = UUID.randomUUID();
        final EmailSettingsModel settings = controller.getSettings(userId);
        assertTrue(settings.isIssueCreated());
        assertTrue(settings.isIssueUpdated());
        assertTrue(settings.isIssueAssigned());
        assertTrue(settings.isIssueMessageCreated());
    }

    @Test
    void isEnabled_SUCCESS_mapsEventTypesToSettings() {
        final UUID userId = UUID.randomUUID();
        controller.updateSettings(userId, ImmutableEmailSettingsJson.builder()
            .issueCreated(false)
            .issueAssigned(false)
            .issueMessageCreated(false)
            .build());

        assertFalse(controller.isEnabled(userId, IssueEventType.ISSUE_CREATED));
        assertTrue(controller.isEnabled(userId, IssueEventType.ISSUE_UPDATED));
        assertFalse(controller.isEnabled(userId, IssueEventType.ISSUE_ASSIGNED));
        assertFalse(controller.isEnabled(userId, IssueEventType.CHAT_MESSAGE_CREATED));
        // not configurable event types are always enabled
        assertTrue(controller.isEnabled(userId, IssueEventType.ORDER_PLACED));
        assertTrue(controller.isEnabled(null, IssueEventType.ISSUE_CREATED));
    }

    @Test
    void deleteSettings_SUCCESS_restoresDefaults() {
        final UUID userId = UUID.randomUUID();
        controller.updateSettings(userId, ImmutableEmailSettingsJson.builder().issueUpdated(false).build());
        assertFalse(controller.isEnabled(userId, IssueEventType.ISSUE_UPDATED));

        controller.deleteSettings(userId);
        assertTrue(controller.isEnabled(userId, IssueEventType.ISSUE_UPDATED));
    }

}
