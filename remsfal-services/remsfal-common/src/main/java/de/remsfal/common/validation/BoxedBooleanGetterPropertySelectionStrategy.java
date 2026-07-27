package de.remsfal.common.validation;

import org.hibernate.validator.spi.properties.ConstrainableExecutable;
import org.hibernate.validator.spi.properties.GetterPropertySelectionStrategy;

import java.beans.Introspector;
import java.util.List;
import java.util.Optional;

/**
 * Hibernate Validator's {@link org.hibernate.validator.internal.properties.DefaultGetterPropertySelectionStrategy}
 * only recognizes {@code isXxx()}/{@code hasXxx()} as JavaBean getters when the return type is the primitive
 * {@code boolean}. Several DTOs in this codebase (e.g. {@code IssueJson.isVisibleToTenants()},
 * {@code OrganizationEmployeeJson.isActive()}) use the boxed {@code Boolean} instead, which means their
 * constraint annotations are silently never evaluated by the default strategy. This strategy additionally
 * accepts {@code Boolean}-returning {@code isXxx()}/{@code hasXxx()} methods as properties.
 *
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public class BoxedBooleanGetterPropertySelectionStrategy implements GetterPropertySelectionStrategy {

    private static final String GET = "get";
    private static final String IS = "is";
    private static final String HAS = "has";

    @Override
    public Optional<String> getProperty(final ConstrainableExecutable executable) {
        if (executable.getParameterTypes().length != 0) {
            return Optional.empty();
        }
        final String name = executable.getName();
        final Class<?> returnType = executable.getReturnType();
        if (name.startsWith(GET) && returnType != void.class) {
            return Optional.of(Introspector.decapitalize(name.substring(GET.length())));
        }
        if (name.startsWith(IS) && (returnType == boolean.class || returnType == Boolean.class)) {
            return Optional.of(Introspector.decapitalize(name.substring(IS.length())));
        }
        if (name.startsWith(HAS) && (returnType == boolean.class || returnType == Boolean.class)) {
            return Optional.of(Introspector.decapitalize(name.substring(HAS.length())));
        }
        return Optional.empty();
    }

    @Override
    public List<String> getGetterMethodNameCandidates(final String propertyName) {
        final String capitalized = Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
        return List.of(GET + capitalized, IS + capitalized, HAS + capitalized);
    }

}
