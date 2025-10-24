package de.remsfal.core.json.ticketing;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.ImmutableStyle;
import de.remsfal.core.json.UserJson;
import de.remsfal.core.model.CustomerModel;
import de.remsfal.core.model.ticketing.IssueModel;
import de.remsfal.core.model.ticketing.IssueModel.Status;
import de.remsfal.core.model.ticketing.IssueModel.Type;
import jakarta.annotation.Nullable;

import java.util.UUID;

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

    public abstract UUID getId();

    public abstract String getName();

    public abstract String getTitle();

    public abstract Type getType();

    public abstract Status getStatus();

    @Nullable
    public abstract UUID getOwner();

    @Nullable
    public abstract String getOwnerName();

    /**
     * Generates a display name for a user.
     * If firstName and lastName are available, returns "firstName lastName".
     * Otherwise, falls back to email.
     *
     * @param user the user model
     * @return the display name
     */
    public static String generateDisplayName(final CustomerModel user) {
        if (user == null) {
            return null;
        }
        return buildDisplayName(user.getFirstName(), user.getLastName(), user.getEmail());
    }

    /**
     * Generate a display name from UserJson (for REST client responses).
     * Prioritizes firstName + lastName, falls back to email.
     *
     * @param user the user JSON
     * @return the display name
     */
    public static String generateDisplayName(final UserJson user) {
        if (user == null) {
            return null;
        }
        return buildDisplayName(user.getFirstName(), user.getLastName(), user.getEmail());
    }

    /**
     * Common logic for building a display name from name components.
     *
     * @param firstName the first name
     * @param lastName the last name
     * @param email the email address (fallback)
     * @return the display name
     */
    private static String buildDisplayName(final String firstName, final String lastName, final String email) {
        // If both firstName and lastName are present, use them
        if (firstName != null && !firstName.isBlank() &&
            lastName != null && !lastName.isBlank()) {
            return String.format("%s %s", firstName, lastName).trim();
        }

        // If only firstName is present
        if (firstName != null && !firstName.isBlank()) {
            return firstName.trim();
        }

        // If only lastName is present
        if (lastName != null && !lastName.isBlank()) {
            return lastName.trim();
        }

        // Fallback to email
        return email;
    }

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

    public static IssueItemJson valueOf(final IssueModel model, final String ownerName) {
        return ImmutableIssueItemJson.builder()
            .id(model.getId())
            .name(model.getTitle())
            .title(model.getTitle())
            .type(model.getType())
            .status(model.getStatus())
            .owner(model.getOwnerId())
            .ownerName(ownerName)
            .build();
    }

}
