package de.remsfal.core.json;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import de.remsfal.core.model.ProjectTreeNodeModel;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
@Schema(description = "A tree node representing a project entity")
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class ProjectTreeNodeJson {

    @Schema(description = "Key of the node", required = true, examples = "Property 1")
    public abstract String getKey();

    @Schema(description = "Data encapsulating node attributes")
    public abstract NodeDataJson getData();

    @Schema(description = "Children nodes")
    public abstract List<ProjectTreeNodeJson> getChildren();

    public static ProjectTreeNodeJson valueOf(ProjectTreeNodeModel treeNode) {
        ImmutableProjectTreeNodeJson.Builder builder = ImmutableProjectTreeNodeJson.builder()
                .key(treeNode.getKey())
                .data(ImmutableNodeDataJson.builder()
                        .type(treeNode.getData().getType())
                        .title(treeNode.getData().getTitle())
                        .description(treeNode.getData().getDescription())
                        .tenant(treeNode.getData().getTenant())
                        .usableSpace(treeNode.getData().getUsableSpace())
                        .build());

        for (ProjectTreeNodeModel child : treeNode.getChildren()) {
            builder.addChildren(valueOf(child));
        }

        return builder.build();
    }
}
