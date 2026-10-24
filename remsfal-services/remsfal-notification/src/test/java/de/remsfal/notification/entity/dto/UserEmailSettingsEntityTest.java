package de.remsfal.notification.entity.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

class UserEmailSettingsEntityTest {

    private static final UUID USER_ID = UUID.randomUUID();

    private static UserEmailSettingsEntity createEntity() {
        final UserEmailSettingsEntity entity = new UserEmailSettingsEntity();
        entity.setUserId(USER_ID);
        return entity;
    }

    @Test
    void newEntity_SUCCESS_allEmailsEnabledByDefault() {
        final UserEmailSettingsEntity entity = createEntity();
        assertEquals(USER_ID, entity.getUserId());
        assertTrue(entity.isIssueCreated());
        assertTrue(entity.isIssueUpdated());
        assertTrue(entity.isIssueAssigned());
        assertTrue(entity.isIssueMessageCreated());
    }

    @Test
    void setters_SUCCESS_disableEmails() {
        final UserEmailSettingsEntity entity = createEntity();
        entity.setIssueCreated(false);
        entity.setIssueUpdated(false);
        entity.setIssueAssigned(false);
        entity.setIssueMessageCreated(false);
        assertFalse(entity.isIssueCreated());
        assertFalse(entity.isIssueUpdated());
        assertFalse(entity.isIssueAssigned());
        assertFalse(entity.isIssueMessageCreated());
    }

    @Test
    void equals_SUCCESS_sameValues() {
        final UserEmailSettingsEntity entity = createEntity();
        assertEquals(entity, entity);
        assertEquals(entity, createEntity());
        assertEquals(entity.hashCode(), createEntity().hashCode());
        assertNotEquals(entity, null);
        assertNotEquals(entity, "no entity");
    }

    @Test
    void equals_FAILED_differentValues() {
        assertDiffers(e -> e.setUserId(UUID.randomUUID()));
        assertDiffers(e -> e.setIssueCreated(false));
        assertDiffers(e -> e.setIssueUpdated(false));
        assertDiffers(e -> e.setIssueAssigned(false));
        assertDiffers(e -> e.setIssueMessageCreated(false));
        assertDiffers(UserEmailSettingsEntity::created);
    }

    private static void assertDiffers(final Consumer<UserEmailSettingsEntity> modification) {
        final UserEmailSettingsEntity other = createEntity();
        modification.accept(other);
        assertNotEquals(createEntity(), other);
    }

}
