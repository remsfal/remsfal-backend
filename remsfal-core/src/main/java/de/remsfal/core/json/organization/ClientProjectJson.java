package de.remsfal.core.json.organization;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.ImmutableStyle;
import de.remsfal.core.json.AddressJson;
import de.remsfal.core.model.project.ProjectModel;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value;

import java.util.UUID;

@Value.Immutable
@ImmutableStyle
@Schema(description = "A project in which a contractor organization is registered, with its billing data")
@JsonDeserialize(as = ImmutableClientProjectJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class ClientProjectJson {

    @NotNull
    @Schema(description = "Unique identifier of the project")
    public abstract UUID getId();

    @NotNull
    public abstract String getTitle();

    @Nullable
    @Schema(description = "Billing recipient (Leistungsempfänger) of the project")
    public abstract String getOwner();

    @Nullable
    @Schema(description = "Representative of the billing recipient (c/o)")
    public abstract String getCareOf();

    @Nullable
    public abstract AddressJson getBillingAddress();

    /**
     * Create a client representation from a project.
     *
     * @param model the project
     * @return the JSON representation
     */
    public static ClientProjectJson valueOf(final ProjectModel model) {
        final ImmutableClientProjectJson.Builder builder = ImmutableClientProjectJson.builder()
            .id(model.getId())
            .title(model.getTitle())
            .owner(model.getOwner())
            .careOf(model.getCareOf());
        if (model.getBillingAddress() != null) {
            builder.billingAddress(AddressJson.valueOf(model.getBillingAddress()));
        }
        return builder.build();
    }
}
