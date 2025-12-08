package de.remsfal.core.json.tenancy;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.ImmutableStyle;
import de.remsfal.core.model.project.TenancyModel;

/**
 * @author Carl Rix [carl.rix@student.htw-berlin.de]
 */
@Immutable
@ImmutableStyle
@Schema(description = "A list of tenancies for a project")
@JsonDeserialize(as = ImmutableProjectTenancyListJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class ProjectTenancyListJson {

    public abstract List<TenancyInfoJson> getTenancies();

    public static ProjectTenancyListJson valueOf(final List<? extends TenancyModel> tenancies) {
        return ImmutableProjectTenancyListJson.builder()
            .tenancies(tenancies.stream()
                .map(TenancyInfoJson::valueOf)
                .collect(Collectors.toList()))
            .build();
    }

}
