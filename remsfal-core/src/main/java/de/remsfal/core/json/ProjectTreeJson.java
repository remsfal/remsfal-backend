package de.remsfal.core.json;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import de.remsfal.core.model.ProjectTreeNodeModel;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
@Schema(description = "A paginated list of project tree nodes")
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class ProjectTreeJson {

    @Schema(
            description = "Index of the first element in the list of available entries, starting at 1",
            required = true,
            examples = "1"
    )
    public abstract Integer getFirst();

    @Schema(
            description = "Number of elements in property list",
            minimum = "1",
            maximum = "100",
            defaultValue = "10",
            required = true
    )
    public abstract Integer getSize();

    @Schema(
            description = "Total number of available elements",
            required = true
    )
    public abstract Long getTotal();

    @Schema(
            description = "List of tree nodes",
            required = true
    )
    public abstract List<ProjectTreeNodeJson> getNodes();

    public static ProjectTreeJson valueOf(
            final List<ProjectTreeNodeModel> treeNodes,
            final Integer first,
            final Long total
    ) {
        ImmutableProjectTreeJson.Builder builder = ImmutableProjectTreeJson.builder()
                .size(treeNodes.size())
                .first(first)
                .total(total);

        for (ProjectTreeNodeModel treeNode : treeNodes) {
            builder.addNodes(ProjectTreeNodeJson.valueOf(treeNode));
        }

        return builder.build();
    }
}
