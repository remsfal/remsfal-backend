package de.remsfal.core.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * Validation annotation to ensure that at least one rent unit list is not empty.
 * Used on RentalAgreementJson to validate that at least one type of rent is specified.
 *
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AtLeastOneRentUnitValidator.class)
@Documented
public @interface AtLeastOneRentUnit {

    String message() default "At least one rent unit is required";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
