package de.remsfal.common.validation;

import io.quarkus.hibernate.validator.ValidatorFactoryCustomizer;
import jakarta.inject.Singleton;
import org.hibernate.validator.BaseHibernateValidatorConfiguration;

/**
 * Registers {@link BoxedBooleanGetterPropertySelectionStrategy} with Hibernate Validator. Discovered
 * automatically by the Quarkus Hibernate Validator extension in every service depending on
 * {@code remsfal-common}.
 *
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Singleton
public class BoxedBooleanGetterValidatorFactoryCustomizer implements ValidatorFactoryCustomizer {

    @Override
    public void customize(final BaseHibernateValidatorConfiguration<?> configuration) {
        configuration.getterPropertySelectionStrategy(new BoxedBooleanGetterPropertySelectionStrategy());
    }

}
