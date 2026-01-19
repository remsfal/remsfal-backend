package de.remsfal.core.json.project;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import de.remsfal.core.ImmutableStyle;
import de.remsfal.core.model.project.ProjectOrganizationModel;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Value.Immutable
@ImmutableStyle
@Schema(description = "List of organizations assigned to a project")
@JsonDeserialize(as = ImmutableProjectOrganizationListJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class ProjectOrganizationListJson {

    public abstract List<ProjectOrganizationJson> getOrganizations();

    public static ProjectOrganizationListJson valueOfSet(Set<? extends ProjectOrganizationModel> models) {
        if (models == null) {
            return ImmutableProjectOrganizationListJson.builder().build();
        }
        return ImmutableProjectOrganizationListJson.builder()
            .organizations(models.stream()
                .map(ProjectOrganizationJson::valueOf)
                .collect(Collectors.toList()))
            .build();
    }
}
