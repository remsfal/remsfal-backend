package de.remsfal.core.json;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import de.remsfal.core.model.ProjectTreeNodeModel;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
@Schema(description = "A tree node representing a project entity")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public abstract class ProjectTreeNodeJson {

    @Schema(description = "Key of the node", required = true, example = "Property 1")
    public abstract String getKey();

    @Schema(description = "Type of the node (e.g., 'property', 'building')", required = true, example = "property")
    public abstract String getType();

    @Schema(description = "Title of the node", example = "Main Building")
    public abstract String getTitle();

    @Schema(description = "Description of the node", example = "A multi-story office building")
    public abstract String getDescription();

    @Schema(description = "Name of the tenant associated with this node", example = "Doe, John")
    public abstract String getTenant();

    @Schema(description = "Usable space in square meters", example = "350.5")
    public abstract float getUsableSpace();

    @Schema(description = "Children nodes")
    public abstract List<ProjectTreeNodeJson> getChildren();

    public static ProjectTreeNodeJson valueOf(ProjectTreeNodeModel treeNode) {
        ImmutableProjectTreeNodeJson.Builder builder = ImmutableProjectTreeNodeJson.builder()
                .key(treeNode.getKey())
                .type(treeNode.getType())
                .title(treeNode.getTitle())
                .description(treeNode.getDescription())
                .tenant(treeNode.getTenant())
                .usableSpace(treeNode.getUsableSpace());

        for (ProjectTreeNodeModel child : treeNode.getChildren()) {
            builder.addChildren(valueOf(child));
        }

        return builder.build();
    }
}
