package de.remsfal.service.boundary.tenancy;

import jakarta.inject.Inject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.remsfal.core.api.tenancy.TenancyEndpoint;
import de.remsfal.core.json.AddressJson;
import de.remsfal.core.json.RentalUnitJson;
import de.remsfal.core.json.tenancy.TenancyListJson;
import de.remsfal.core.model.project.RentalAgreementModel;
import de.remsfal.service.control.ProjectController;
import de.remsfal.service.control.PropertyController;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public class TenancyResource extends AbstractTenancyResource implements TenancyEndpoint {

    @Inject
    ProjectController projectController;

    @Inject
    PropertyController propertyController;

    @Override
    public TenancyListJson getTenancies() {
        final List<? extends RentalAgreementModel> agreements = agreementController.getRentalAgreements(principal);
        final Map<UUID, RentalUnitJson> rentalUnitsMap = new HashMap<>();
        final Map<UUID, String> projectTitleMap = new HashMap<>();
        final Map<UUID, AddressJson> unitAddressMap = new HashMap<>();

        agreements.stream()
            .map(RentalAgreementModel::getProjectId)
            .distinct()
            .forEach(pid -> {
                rentalUnitsMap.putAll(propertyController.getRentalUnitsMapForProject(pid));
                unitAddressMap.putAll(propertyController.getUnitAddressMapForProject(pid));
                projectTitleMap.put(pid, projectController.getProjectTitle(pid));
            });

        return TenancyListJson.valueOf(agreements, rentalUnitsMap, projectTitleMap, unitAddressMap);
    }

}
