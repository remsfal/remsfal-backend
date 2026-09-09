package de.remsfal.core.json.eventing;

import java.util.UUID;

import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.ImmutableStyle;
import jakarta.annotation.Nullable;

@Immutable
@ImmutableStyle
@JsonDeserialize(as = ImmutableProjectEventJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public interface ProjectEventJson {

    String TOPIC = "project-events";

    enum ProjectEventType {
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
}
