package de.remsfal.core.json.organization;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import de.remsfal.core.ImmutableStyle;
import de.remsfal.core.model.OrganizationEmployeeModel;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
@ImmutableStyle
@Schema(description = "A list of organization employees")
@JsonDeserialize(as = ImmutableOrganizationEmployeeListJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class OrganizationEmployeeListJson {

    @NotNull
    public abstract List<OrganizationEmployeeJson> getEmployees();

    public static OrganizationEmployeeListJson valueOfList(List<? extends OrganizationEmployeeModel> models) {
        final ImmutableOrganizationEmployeeListJson.Builder builder = ImmutableOrganizationEmployeeListJson.builder();
        for (OrganizationEmployeeModel model : models) {
            builder.addEmployees(OrganizationEmployeeJson.valueOf(model));
        }
        return builder.build();
    }
}
