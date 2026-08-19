package de.remsfal.core.validation;

import de.remsfal.core.json.project.RentalAgreementJson;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator implementation for {@link NoRentsOnPatch} annotation. Checks that none of the rent unit
 * lists (apartmentRents, buildingRents, propertyRents, siteRents, storageRents, commercialRents) are
 * populated.
 *
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public class NoRentsOnPatchValidator implements ConstraintValidator<NoRentsOnPatch, RentalAgreementJson> {

    @Override
    public boolean isValid(RentalAgreementJson value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        return isEmpty(value.getApartmentRents())
            && isEmpty(value.getBuildingRents())
            && isEmpty(value.getPropertyRents())
            && isEmpty(value.getSiteRents())
            && isEmpty(value.getStorageRents())
            && isEmpty(value.getCommercialRents());
    }

    private boolean isEmpty(java.util.List<?> list) {
        return list == null || list.isEmpty();
    }
}
