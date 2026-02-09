package de.remsfal.service.boundary.tenancy;

import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;

import java.util.UUID;

import de.remsfal.core.api.tenancy.TenancyEndpoint;
import de.remsfal.service.control.ApartmentController;
import de.remsfal.service.control.BuildingController;
import de.remsfal.service.control.CommercialController;
import de.remsfal.service.control.PropertyController;
import de.remsfal.service.control.SiteController;
import de.remsfal.service.control.StorageController;
import de.remsfal.service.entity.dto.ApartmentRentEntity;
import de.remsfal.service.entity.dto.BuildingRentEntity;
import de.remsfal.service.entity.dto.CommercialRentEntity;
import de.remsfal.service.entity.dto.PropertyRentEntity;
import de.remsfal.service.entity.dto.SiteRentEntity;
import de.remsfal.service.entity.dto.StorageRentEntity;
import de.remsfal.service.entity.dto.RentalAgreementEntity;

import de.remsfal.core.json.tenancy.ImmutableTenancyListJson;
import de.remsfal.core.json.tenancy.TenancyItemJson;
import de.remsfal.core.json.tenancy.TenancyJson;
import de.remsfal.core.json.tenancy.TenancyListJson;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public class TenancyResource extends AbstractTenancyResource implements TenancyEndpoint {

    @Inject
    PropertyController propertyController;

    @Inject
    SiteController siteController;

    @Inject
    BuildingController buildingController;

    @Inject
    ApartmentController apartmentController;

    @Inject
    CommercialController commercialController;

    @Inject
    StorageController storageController;

    @Override
    public TenancyListJson getTenancies() {
        final ImmutableTenancyListJson.Builder rentBuilder = ImmutableTenancyListJson.builder();
        for(RentalAgreementEntity agreement : agreementController.getRentalAgreements(principal)) {
            rentBuilder.addAllRentalAgreements(agreement.getPropertyRents()
                .stream().map(rent -> {
                    return TenancyItemJson.valueOf(agreement,
                        propertyController.getProperty(agreement.getProjectId(), rent.getPropertyId()));
                })
                .toList());
            rentBuilder.addAllRentalAgreements(agreement.getSiteRents()
                .stream().map(rent -> {
                    return TenancyItemJson.valueOf(agreement,
                        siteController.getSite(agreement.getProjectId(), rent.getSiteId()));
                })
                .toList());
            rentBuilder.addAllRentalAgreements(agreement.getBuildingRents()
                .stream().map(rent -> {
                    return TenancyItemJson.valueOf(agreement,
                        buildingController.getBuilding(agreement.getProjectId(), rent.getBuildingId()));
                })
                .toList());
            rentBuilder.addAllRentalAgreements(agreement.getApartmentRents()
                .stream().map(rent -> {
                    return TenancyItemJson.valueOf(agreement,
                        apartmentController.getApartment(agreement.getProjectId(), rent.getApartmentId()));
                })
                .toList());
            rentBuilder.addAllRentalAgreements(agreement.getStorageRents()
                .stream().map(rent -> {
                    return TenancyItemJson.valueOf(agreement,
                        storageController.getStorage(agreement.getProjectId(), rent.getStorageId()));
                })
                .toList());
            rentBuilder.addAllRentalAgreements(agreement.getCommercialRents()
                .stream().map(rent -> {
                    return TenancyItemJson.valueOf(agreement,
                        commercialController.getCommercial(agreement.getProjectId(), rent.getCommercialId()));
                })
                .toList());
        }
        return rentBuilder.build();
    }

    @Override
    public TenancyJson getPropertyTenancy(final UUID tenancyId, final UUID rentalId) {
        checkReadPermissions(tenancyId);
        final RentalAgreementEntity agreement = agreementController.getRentalAgreement(principal, tenancyId);
        PropertyRentEntity rent = agreement.getPropertyRents().stream()
            .filter(r -> rentalId.equals(r.getPropertyId()))
            .findFirst()
            .orElseThrow(() -> new NotFoundException("Unable to find rent"));
        return TenancyJson.valueOf(agreement, rent,
            propertyController.getProperty(agreement.getProjectId(), rentalId));
    }

    @Override
    public TenancyJson getSiteTenancy(final UUID tenancyId, final UUID rentalId) {
        checkReadPermissions(tenancyId);
        final RentalAgreementEntity agreement = agreementController.getRentalAgreement(principal, tenancyId);
        SiteRentEntity rent = agreement.getSiteRents().stream()
            .filter(r -> rentalId.equals(r.getSiteId()))
            .findFirst()
            .orElseThrow(() -> new NotFoundException("Unable to find rent"));
        return TenancyJson.valueOf(agreement, rent, siteController.getSite(agreement.getProjectId(), rentalId));
    }

    @Override
    public TenancyJson getBuildingTenancy(final UUID tenancyId, final UUID rentalId) {
        checkReadPermissions(tenancyId);
        final RentalAgreementEntity agreement = agreementController.getRentalAgreement(principal, tenancyId);
        BuildingRentEntity rent = agreement.getBuildingRents().stream()
            .filter(r -> rentalId.equals(r.getBuildingId()))
            .findFirst()
            .orElseThrow(() -> new NotFoundException("Unable to find rent"));
        return TenancyJson.valueOf(agreement, rent,
            buildingController.getBuilding(agreement.getProjectId(), rentalId));
    }

    @Override
    public TenancyJson getApartmentTenancy(final UUID tenancyId, final UUID rentalId) {
        checkReadPermissions(tenancyId);
        final RentalAgreementEntity agreement = agreementController.getRentalAgreement(principal, tenancyId);
        ApartmentRentEntity rent = agreement.getApartmentRents().stream()
            .filter(r -> rentalId.equals(r.getApartmentId()))
            .findFirst()
            .orElseThrow(() -> new NotFoundException("Unable to find rent"));
        return TenancyJson.valueOf(agreement, rent,
            apartmentController.getApartment(agreement.getProjectId(), rentalId));
    }

    @Override
    public TenancyJson getStorageTenancy(final UUID tenancyId, final UUID rentalId) {
        checkReadPermissions(tenancyId);
        final RentalAgreementEntity agreement = agreementController.getRentalAgreement(principal, tenancyId);
        StorageRentEntity rent = agreement.getStorageRents().stream()
            .filter(r -> rentalId.equals(r.getStorageId()))
            .findFirst()
            .orElseThrow(() -> new NotFoundException("Unable to find rent"));
        return TenancyJson.valueOf(agreement, rent,
            storageController.getStorage(agreement.getProjectId(), rentalId));
    }

    @Override
    public TenancyJson getCommercialTenancy(final UUID tenancyId, final UUID rentalId) {
        checkReadPermissions(tenancyId);
        final RentalAgreementEntity agreement = agreementController.getRentalAgreement(principal, tenancyId);
        CommercialRentEntity rent = agreement.getCommercialRents().stream()
            .filter(r -> rentalId.equals(r.getCommercialId()))
            .findFirst()
            .orElseThrow(() -> new NotFoundException("Unable to find rent"));
        return TenancyJson.valueOf(agreement, rent,
            commercialController.getCommercial(agreement.getProjectId(), rentalId));
    }

}