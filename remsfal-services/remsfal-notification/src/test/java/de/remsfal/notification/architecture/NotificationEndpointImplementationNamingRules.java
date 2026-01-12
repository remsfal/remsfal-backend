package de.remsfal.notification.architecture;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

public final class NotificationEndpointImplementationNamingRules {
    private NotificationEndpointImplementationNamingRules() {}

    private static final DescribedPredicate<JavaClass> IMPLEMENTS_ENDPOINT_INTERFACE =
            new DescribedPredicate<>("implement *Endpoint interface") {
                @Override
                public boolean test(JavaClass javaClass) {
                    return javaClass.getAllRawInterfaces().stream()
                            .anyMatch(i -> i.getSimpleName().endsWith("Endpoint"));
                }
            };

    @ArchTest
    static final ArchRule endpoint_implementations_should_end_with_resource =
            classes()
                    .that().areTopLevelClasses()
                    .and().resideInAnyPackage("de.remsfal.notification.boundary..")
                    .and(IMPLEMENTS_ENDPOINT_INTERFACE)
                    .should().haveSimpleNameEndingWith("Resource")
                    .allowEmptyShould(true);
}
