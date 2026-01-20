package de.remsfal.ticketing.architecture;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

/**
 * Naming rules for endpoint implementation classes in the Ticketing service.
 * These rules ensure a clear separation between endpoint interfaces
 * (API contracts) and their concrete implementations.
 * Endpoint interfaces typically define the external API,
 * while implementation classes represent concrete JAX-RS resources.
 */
public final class TicketingEndpointImplementationNamingRules {
    /**
     * Utility class – not meant to be instantiated.
     */
    private TicketingEndpointImplementationNamingRules() {}

    /**
     * Predicate that matches classes implementing an endpoint interface.
     * A class is considered an endpoint implementation if it implements
     * at least one interface whose simple name ends with {@code Endpoint}.
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
     * follow the naming convention {@code *Resource}.
     * This rule enforces:
     * - a clear distinction between API interfaces (*Endpoint)
     * - and their concrete REST resource implementations (*Resource)
     * Only top-level classes in the ticketing boundary package are considered.
     */
    @ArchTest
    static final ArchRule endpoint_implementations_should_end_with_resource =
            classes()
                    .that().areTopLevelClasses()
                    .and().resideInAnyPackage("de.remsfal.ticketing.boundary..")
                    .and(IMPLEMENTS_ENDPOINT_INTERFACE)
                    .should().haveSimpleNameEndingWith("Resource")
                    .allowEmptyShould(true);
}
