package de.remsfal.core.json.ticketing;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.ImmutableStyle;
import de.remsfal.core.model.ticketing.IssueModel;
import de.remsfal.core.model.ticketing.IssueModel.Status;
import de.remsfal.core.model.ticketing.IssueModel.Type;
import jakarta.annotation.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value.Immutable;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Immutable
@ImmutableStyle
@Schema(description = "An issue item with basic information")
@JsonDeserialize(as = ImmutableIssueItemJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class IssueItemJson {
    // Validation is not required, because it is read-only.

    public abstract String getId();

    public abstract String getName();

    public abstract String getTitle();

    public abstract Type getType();

    public abstract Status getStatus();

    @Nullable
    public abstract String getOwner();

    public static IssueItemJson valueOf(final IssueModel model) {
        return ImmutableIssueItemJson.builder()
            .id(model.getId())
            .name(model.getTitle())
            .title(model.getTitle())
            .type(model.getType())
            .status(model.getStatus())
            .owner(model.getOwnerId())
            .build();
    }

}
