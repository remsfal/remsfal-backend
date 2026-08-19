package de.remsfal.core.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * Validation annotation to ensure that no rent unit list is populated. Used on RentalAgreementJson to
 * reject PATCH requests that still try to modify rents, which is only allowed via the dedicated
 * add/delete rent endpoints.
 *
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NoRentsOnPatchValidator.class)
@Documented
public @interface NoRentsOnPatch {

    String message() default "Rents can no longer be modified via this endpoint; "
        + "use POST/DELETE .../{rentalUnitType}/{rentalUnitId} instead";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
