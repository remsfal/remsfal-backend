package de.remsfal.core.dto;

import javax.validation.constraints.NotNull;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Value.Immutable
@Schema(description = "A list of project members")
@JsonDeserialize(as = ImmutableProjectMemberListJson.class)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public abstract class ProjectMemberListJson {

    @NotNull
    public abstract List<ProjectMemberJson> getMembers();

    // TODO: pagination
}
