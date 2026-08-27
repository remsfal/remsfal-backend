package de.remsfal.core.json.eventing;

import java.util.List;
import java.util.UUID;

import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.ImmutableStyle;
import de.remsfal.core.json.organization.OrganizationJson;
import jakarta.annotation.Nullable;

@Immutable
@ImmutableStyle
@JsonDeserialize(as = ImmutableOrganizationEventJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public interface OrganizationEventJson {

    String TOPIC = "organization-events";

    enum OrganizationEventType {
        ORGANIZATION_UPDATED
    }

    OrganizationEventType getOrganizationEventType();

    UUID getOrganizationId();

    /**
     * The updated organization profile. Only present for {@link OrganizationEventType#ORGANIZATION_UPDATED}.
     */
    @Nullable
    OrganizationJson getOrganization();

    /**
     * The ID of the user who performed the change that triggered this event.
     */
    @Nullable
    UUID getChangedByUserId();

    /**
     * The display name of the user who performed the change that triggered this event.
     */
    @Nullable
    String getChangedByName();

    /**
     * Contractor records linked to this organization, each with the project it belongs to. Only present
     * for {@link OrganizationEventType#ORGANIZATION_UPDATED}. May be empty if the organization has no
     * linked contractors; consumers decide whether that implies any action.
     */
    @Nullable
    List<AffectedContractorJson> getAffectedContractors();
}
