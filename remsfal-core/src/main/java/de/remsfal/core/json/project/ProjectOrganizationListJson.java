package de.remsfal.core.json.project;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import de.remsfal.core.ImmutableStyle;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value;

import java.util.List;

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

    public static ProjectOrganizationListJson valueOf(final List<ProjectOrganizationJson> organizations) {
        return ImmutableProjectOrganizationListJson.builder()
            .organizations(organizations)
            .build();
    }
}
