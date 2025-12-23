package de.remsfal.core.json;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import de.remsfal.core.ImmutableStyle;
import de.remsfal.core.model.OrganizationModel;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value;

import java.util.ArrayList;
import java.util.List;

@Value.Immutable
@ImmutableStyle
@Schema(description = "A list of organizations")
@JsonDeserialize(as = ImmutableOrganizationListJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class OrganizationListJson {

    @NotNull
    public abstract List<OrganizationJson> getOrganizations();

    public abstract Integer getOffset();

    public abstract Long getTotal();

    public static OrganizationListJson valueOf(List<? extends OrganizationModel> organizations, Integer offset,
        Long total) {
        ImmutableOrganizationListJson.Builder builder = ImmutableOrganizationListJson.builder();

        for (OrganizationModel organization : organizations) {
            builder.addOrganizations(OrganizationJson.valueOf(organization));
        }

        return builder.offset(offset).total(total).build();
    }

    public static OrganizationListJson valueOf(List<? extends OrganizationModel> organizations) {
        ImmutableOrganizationListJson.Builder builder = ImmutableOrganizationListJson.builder();

        for (OrganizationModel organization : organizations) {
            builder.addOrganizations(OrganizationJson.valueOf(organization));
        }

        return builder.offset(0).total((long) organizations.size()).build();
    }
}
