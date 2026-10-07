package de.remsfal.core.json.organization;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import de.remsfal.core.ImmutableStyle;
import de.remsfal.core.model.project.ProjectModel;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
@ImmutableStyle
@Schema(description = "A paginated list of clients (projects with billing data) of a contractor organization")
@JsonDeserialize(as = ImmutableClientListJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class ClientListJson {

    @NotNull
    public abstract List<ClientProjectJson> getProjects();

    public abstract Integer getOffset();

    @Schema(description = "Total number of projects")
    public abstract Long getTotal();

    public static ClientListJson valueOf(final List<? extends ProjectModel> projects, final Integer offset,
        final Long total) {
        final ImmutableClientListJson.Builder builder = ImmutableClientListJson.builder();
        for (ProjectModel project : projects) {
            builder.addProjects(ClientProjectJson.valueOf(project));
        }
        return builder.offset(offset).total(total).build();
    }
}
