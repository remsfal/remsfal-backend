package de.remsfal.core.json.eventing;

import de.remsfal.core.ImmutableStyle;
import de.remsfal.core.json.UserJson;
import de.remsfal.core.json.organization.OrganizationJson;
import de.remsfal.core.json.project.ProjectJson;

import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import jakarta.annotation.Nullable;

@Immutable
@ImmutableStyle
@JsonDeserialize(as = ImmutableEmailEventJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public interface EmailEventJson {

    String TOPIC = "user-notification";

    enum NotificationEventType {
        USER_REGISTRATION,
        PROJECT_ADMISSION,
        ADDITIONAL_EMAIL_VERIFICATION,
        ORGANIZATION_ADMISSION
    }

    /*
     * User information, including the user's locale
     */
    UserJson getUser();

    /*
     * Type of notification, e.g. registration or project membership
     */
    NotificationEventType getNotificationEventType();

    /*
     * Link
     */
    String getLink();

    /*
     * Project this notification relates to. Only present for PROJECT_ADMISSION.
     */
    @Nullable
    ProjectJson getProject();

    /*
     * Organization this notification relates to. Only present for ORGANIZATION_ADMISSION.
     */
    @Nullable
    OrganizationJson getOrganization();

}
