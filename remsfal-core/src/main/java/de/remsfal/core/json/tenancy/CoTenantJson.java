package de.remsfal.core.json.tenancy;

import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.ImmutableStyle;
import de.remsfal.core.model.tenancy.CoTenantModel;
import jakarta.annotation.Nullable;

/**
 * JSON representation of a tenant in a rental agreement.
 *
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Immutable
@ImmutableStyle
@Schema(description = "Tenant information in a rental agreement")
@JsonDeserialize(as = ImmutableCoTenantJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class CoTenantJson implements CoTenantModel {
    // Validation is not required because it is read-only for tenants.

    @Schema(readOnly = true)
    @Override
    public abstract UUID getId();

    @Nullable
    @Schema(readOnly = true)
    @Override
    public abstract String getFirstName();

    @Nullable
    @Schema(readOnly = true)
    @Override
    public abstract String getLastName();

    @Nullable
    @Schema(readOnly = true)
    @Override
    public abstract UUID getUserId();

    public static ImmutableCoTenantJson valueOf(final CoTenantModel model) {
        if (model == null) {
            return null;
        }
        return ImmutableCoTenantJson.builder()
            .id(model.getId())
            .firstName(model.getFirstName())
            .lastName(model.getLastName())
            .userId(model.getUserId())
            .build();
    }
}
