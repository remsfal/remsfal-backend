package de.remsfal.core.json.notification;

import java.util.UUID;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.ImmutableStyle;
import de.remsfal.core.model.notification.EmailSettingsModel;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Null;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value.Immutable;

/**
 * Email preferences of the authenticated user. On update, only the provided flags are changed.
 */
@Immutable
@ImmutableStyle
@Schema(description = "Email preferences of the authenticated user")
@JsonDeserialize(as = ImmutableEmailSettingsJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class EmailSettingsJson implements EmailSettingsModel {

    @Null
    @Nullable
    @Schema(readOnly = true, description = "Unique identifier of the user")
    @Override
    public abstract UUID getUserId();

    @Nullable
    @Schema(description = "Receive an email when a new issue is created")
    @Override
    public abstract Boolean isIssueCreated();

    @Nullable
    @Schema(description = "Receive an email when an issue is updated")
    @Override
    public abstract Boolean isIssueUpdated();

    @Nullable
    @Schema(description = "Receive an email when an issue is assigned")
    @Override
    public abstract Boolean isIssueAssigned();

    @Nullable
    @Schema(description = "Receive an email when a new message is posted in an issue")
    @Override
    public abstract Boolean isIssueMessageCreated();

    public static EmailSettingsJson valueOf(final EmailSettingsModel model) {
        return ImmutableEmailSettingsJson.builder()
            .userId(model.getUserId())
            .issueCreated(model.isIssueCreated())
            .issueUpdated(model.isIssueUpdated())
            .issueAssigned(model.isIssueAssigned())
            .issueMessageCreated(model.isIssueMessageCreated())
            .build();
    }

}
