package de.remsfal.notification.architecture;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

/**
 * ArchUnit rules enforcing naming conventions for endpoint implementations
 * in the Notification service.
 * These rules ensure that concrete implementations of endpoint interfaces
 * are clearly identifiable and consistently named within the boundary layer.
 */
public final class NotificationEndpointImplementationNamingRules {

    /**
     * Utility class – not meant to be instantiated.
     */
    private NotificationEndpointImplementationNamingRules() {}

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
     * Ensures that endpoint implementation classes are named consistently.
     * Any top-level class located in the boundary package that implements
     * an endpoint interface must have a simple name ending with {@code Resource}.
     * This rule makes it explicit which classes represent concrete REST
     * resource implementations.
     */
    @ArchTest
    static final ArchRule endpoint_implementations_should_end_with_resource =
            classes()
                    .that().areTopLevelClasses()
                    .and().resideInAnyPackage("de.remsfal.notification.boundary..")
                    .and(IMPLEMENTS_ENDPOINT_INTERFACE)
                    .should().haveSimpleNameEndingWith("Resource")
                    .allowEmptyShould(true);
}
