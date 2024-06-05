package de.remsfal.core.json;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import de.remsfal.core.model.ProjectMemberModel;
import de.remsfal.core.model.ProjectModel;
import de.remsfal.core.model.UserModel;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value;

import java.util.Set;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Value.Immutable
@Schema(description = "A project item with the user's member role only")
@JsonDeserialize(as = ImmutableProjectJson.class)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public abstract class ProjectItemJson {

    @NotNull
    public abstract String getId();

    @NotNull
    public abstract String getName();

    @NotNull
    public abstract ProjectMemberModel.UserRole getMemberRole();

    boolean isPrivileged() {
        if(getMemberRole() == null) {
            return false;
        } else {
            return getMemberRole().isPrivileged();
        }
    }

    public static ProjectItemJson valueOf(ProjectModel model, final UserModel user) {
        final ImmutableProjectItemJson.Builder builder = ImmutableProjectItemJson.builder()
            .id(model.getId())
            .name(model.getTitle());
        for(ProjectMemberModel member : model.getMembers()) {
            if(member.getId().equals(user.getId())) {
                builder.memberRole(member.getRole());
            }
        }
        return builder.build();
    }

}
