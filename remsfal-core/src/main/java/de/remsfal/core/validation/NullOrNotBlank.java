package de.remsfal.core.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.ReportAsSingleViolation;

@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER })
@Constraint(validatedBy = NullOrNotBlankValidator.class)
@Retention(RetentionPolicy.RUNTIME)
@ReportAsSingleViolation
public @interface NullOrNotBlank {

    String message() default "{invalid.NullOrNotBlank}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
