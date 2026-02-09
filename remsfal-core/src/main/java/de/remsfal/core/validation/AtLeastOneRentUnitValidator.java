package de.remsfal.core.validation;

import de.remsfal.core.json.project.RentalAgreementJson;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator implementation for {@link AtLeastOneRentUnit} annotation. Checks that at least one rent unit list
 * (apartmentRents, buildingRents, propertyRents, siteRents, storageRents, commercialRents) is not empty.
 *
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public class AtLeastOneRentUnitValidator implements ConstraintValidator<AtLeastOneRentUnit, RentalAgreementJson> {

    @Override
    public boolean isValid(RentalAgreementJson value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        boolean hasAtLeastOneRent = hasElements(value.getApartmentRents())
            || hasElements(value.getBuildingRents())
            || hasElements(value.getPropertyRents())
            || hasElements(value.getSiteRents())
            || hasElements(value.getStorageRents())
            || hasElements(value.getCommercialRents());

        if (!hasAtLeastOneRent) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                "At least one rent unit (apartmentRents, buildingRents, propertyRents, "
                + "siteRents, storageRents, or commercialRents) must be specified")
                .addConstraintViolation();
            return false;
        }

        return true;
    }

    private boolean hasElements(java.util.List<?> list) {
        return list != null && !list.isEmpty();
    }
}
