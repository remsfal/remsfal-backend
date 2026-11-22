package de.remsfal.core.json.eventing;

import java.util.UUID;

import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.ImmutableStyle;
import de.remsfal.core.json.project.ProjectJson;
import de.remsfal.core.json.project.RentalAgreementJson;
import jakarta.annotation.Nullable;

@Immutable
@ImmutableStyle
@JsonDeserialize(as = ImmutableProjectEventJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public interface ProjectEventJson extends EventHeaderJson {

    String TOPIC = "project-events";

    enum ProjectEventType {
        PROJECT_CREATED,
        PROJECT_DELETED,
        RENTAL_AGREEMENT_DELETED
    }

    ProjectEventType getProjectEventType();

    UUID getProjectId();

    /**
     * The deleted rental agreement's id. Only present for {@link ProjectEventType#RENTAL_AGREEMENT_DELETED}.
     */
    @Nullable
    UUID getAgreementId();

    /**
     * The managing organization of the created project. Only present for {@link ProjectEventType#PROJECT_CREATED}
     * and only if the project was created for an organization.
     */
    @Nullable
    UUID getManagingOrganizationId();

    /**
     * The project, for {@link ProjectEventType#PROJECT_DELETED} its last state.
     */
    @Nullable
    ProjectJson getProject();

    /**
     * The last state of the deleted rental agreement. Only present for
     * {@link ProjectEventType#RENTAL_AGREEMENT_DELETED}.
     */
    @Nullable
    RentalAgreementJson getAgreement();
}
