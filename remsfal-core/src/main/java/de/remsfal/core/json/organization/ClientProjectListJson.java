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
@Schema(description = "A list of clients (projects with billing data) of a contractor organization")
@JsonDeserialize(as = ImmutableClientProjectListJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class ClientProjectListJson {

    @NotNull
    public abstract List<ClientProjectJson> getProjects();

    public static ClientProjectListJson valueOf(final List<? extends ProjectModel> projects) {
        final ImmutableClientProjectListJson.Builder builder = ImmutableClientProjectListJson.builder();
        for (ProjectModel project : projects) {
            builder.addProjects(ClientProjectJson.valueOf(project));
        }
        return builder.build();
    }
}
