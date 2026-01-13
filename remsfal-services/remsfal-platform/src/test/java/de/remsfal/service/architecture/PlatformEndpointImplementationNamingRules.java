package de.remsfal.service.architecture;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

/**
 * Naming rules for endpoint implementation classes in the platform (service) module.
 * These rules ensure a clear and consistent distinction between
 * endpoint interfaces (API contracts) and their concrete implementations.
 * Endpoint interfaces typically define the external API,
 * while implementation classes act as JAX-RS resources.
 */
public final class PlatformEndpointImplementationNamingRules {
    /**
     * Utility class – not meant to be instantiated.
     */
    private PlatformEndpointImplementationNamingRules() {}

    /**
     * Predicate that matches classes implementing an interface whose name ends with "Endpoint".
     * This is used to identify concrete endpoint implementation classes,
     * independent of the actual interface package.
     */
    private static final DescribedPredicate<JavaClass> IMPLEMENTS_ENDPOINT_INTERFACE =
            new DescribedPredicate<>("implement *Endpoint interface") {
                @Override
                public boolean test(JavaClass javaClass) {
                    return javaClass.getAllRawInterfaces().stream()
                            .anyMatch(i -> i.getSimpleName().endsWith("Endpoint"));
                }
            };

    /**
     * Ensures that endpoint implementation classes in the boundary layer
     * follow the naming convention "*Resource".
     * This rule enforces:
     * - a clear separation between API interfaces (*Endpoint)
     * - and their concrete JAX-RS resource implementations (*Resource)
     * Only top-level classes in the boundary package are considered.
     */
    @ArchTest
    static final ArchRule endpoint_implementations_should_end_with_resource =
            classes()
                    .that().areTopLevelClasses()
                    .and().resideInAnyPackage("de.remsfal.service.boundary..")
                    .and(IMPLEMENTS_ENDPOINT_INTERFACE)
                    .should().haveSimpleNameEndingWith("Resource")
                    .allowEmptyShould(true);
}
