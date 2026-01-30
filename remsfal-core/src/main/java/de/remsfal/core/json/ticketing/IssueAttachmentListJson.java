package de.remsfal.core.json.ticketing;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.ImmutableStyle;
import de.remsfal.core.model.ticketing.IssueAttachmentModel;

/**
 * @author GitHub Copilot
 */
@Immutable
@ImmutableStyle
@Schema(description = "A list of issue attachments")
@JsonDeserialize(as = ImmutableIssueAttachmentListJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class IssueAttachmentListJson {

    @Schema(description = "List of attachments")
    public abstract List<IssueAttachmentJson> getAttachments();

    public static IssueAttachmentListJson valueOf(final List<? extends IssueAttachmentModel> models) {
        return ImmutableIssueAttachmentListJson.builder()
            .attachments(models.stream()
                .map(IssueAttachmentJson::valueOf)
                .collect(Collectors.toList()))
            .build();
    }

}
