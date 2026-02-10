package de.remsfal.core.json.project;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import de.remsfal.core.ImmutableStyle;
import de.remsfal.core.model.project.RentalAgreementModel;
import de.remsfal.core.model.project.TenantModel;

/**
 * List of tenants with their rental information.
 *
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Immutable
@ImmutableStyle
@Schema(description = "A list of tenants for a project")
@JsonDeserialize(as = ImmutableTenantListJson.class)
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public abstract class TenantListJson {

    public abstract List<TenantItemJson> getTenants();

    /**
     * Creates a TenantListJson from a list of tenants with their rental agreements.
     *
     * @param tenants list of tenants
     * @param agreementsByTenant map of tenant ID to their rental agreements
     * @param rentalUnitsMap map of unit IDs to rental unit JSON objects
     * @return the tenant list JSON
     */
    public static TenantListJson valueOf(
            final List<? extends TenantModel> tenants,
            final Map<UUID, List<? extends RentalAgreementModel>> agreementsByTenant,
            final Map<UUID, RentalUnitJson> rentalUnitsMap) {
        return ImmutableTenantListJson.builder()
            .tenants(tenants.stream()
                .map(tenant -> TenantItemJson.valueOf(
                    tenant,
                    agreementsByTenant.getOrDefault(tenant.getId(), List.of()),
                    rentalUnitsMap))
                .toList())
            .build();
    }

}
