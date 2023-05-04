package de.remsfal.core.dto;

import jakarta.validation.constraints.NotNull;

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
@Schema(description = "A list of projects")
@JsonDeserialize(as = ImmutableProjectListJson.class)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public abstract class ProjectListJson {

    @NotNull
    public abstract List<ProjectJson> getProjects();

    // TODO: pagination
}
