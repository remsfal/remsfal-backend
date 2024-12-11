package de.remsfal.core.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Size;

@NullOrNotBlank
@Size(max=255)
@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER })
@Constraint(validatedBy = {})
@Retention(RetentionPolicy.RUNTIME)
public @interface Title {

    String message() default "{invalid.title}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
