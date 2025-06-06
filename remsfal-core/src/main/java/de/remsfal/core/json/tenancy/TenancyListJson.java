package de.remsfal.core.json.tenancy;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.model.project.TenancyModel;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Value.Immutable
@Schema(description = "A list of tenancies from a tenant's perspective")
@JsonDeserialize(as = ImmutableTenancyListJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class TenancyListJson {
    // Validation is not required, because it is read-only for tenants.

    public abstract List<TenancyItemJson> getTenancies();

    public static TenancyListJson valueOf(final List<? extends TenancyModel> tenancies) {
        final ImmutableTenancyListJson.Builder builder = ImmutableTenancyListJson.builder();
        for(TenancyModel tenancy : tenancies) {
            builder.addTenancies(TenancyItemJson.valueOf(tenancy));
        }
        return builder.build();
    }

}
