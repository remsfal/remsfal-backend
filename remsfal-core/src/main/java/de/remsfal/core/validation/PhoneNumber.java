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
@Pattern(regexp = "^$|^\\+[1-9]\\d{4,14}$")
@ReportAsSingleViolation
public @interface PhoneNumber {

    String message() default "The number must be empty or match the E.164 formatted phone numbers";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
