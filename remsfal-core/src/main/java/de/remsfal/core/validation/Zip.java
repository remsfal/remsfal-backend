package de.remsfal.core.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.ReportAsSingleViolation;
import jakarta.validation.constraints.Pattern;

@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER })
@Constraint(validatedBy = {})
@Retention(RetentionPolicy.RUNTIME)
@Pattern(regexp = "^\\d{4,5}$")
@ReportAsSingleViolation
public @interface Zip {
    String message() default "{invalid.zip}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
