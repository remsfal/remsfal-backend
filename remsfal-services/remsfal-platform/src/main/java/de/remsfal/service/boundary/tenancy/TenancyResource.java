package de.remsfal.service.boundary.tenancy;

import jakarta.inject.Inject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.remsfal.core.api.tenancy.TenancyEndpoint;
import de.remsfal.core.json.RentalUnitJson;
import de.remsfal.core.json.tenancy.TenancyListJson;
import de.remsfal.service.control.PropertyController;
import de.remsfal.service.entity.dto.RentalAgreementEntity;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public class TenancyResource extends AbstractTenancyResource implements TenancyEndpoint {

    @Inject
    PropertyController propertyController;

    @Override
    public TenancyListJson getTenancies() {
        final List<RentalAgreementEntity> agreements = agreementController.getRentalAgreements(principal);
        final Map<UUID, RentalUnitJson> rentalUnitsMap = new HashMap<>();
        agreements.stream()
            .map(RentalAgreementEntity::getProjectId)
            .distinct()
            .forEach(pid -> rentalUnitsMap.putAll(propertyController.getRentalUnitsMapForProject(pid)));
        return TenancyListJson.valueOf(agreements, rentalUnitsMap);
    }

}
