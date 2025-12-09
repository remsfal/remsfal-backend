package de.remsfal.test.architecture;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaType;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

/**
 * Enforces naming conventions for classes implementing {@code *Endpoint}
 * interfaces.
 * *
 * Any top-level class located in a boundary package that implements
 * an interface whose name ends with {@code Endpoint} must itself
 * have a class name ending with {@code Resource}.
 * *
 * This rule helps distinguish between endpoint interfaces (API contracts)
 * and their concrete implementations.
 */
@AnalyzeClasses(
        packages = "de.remsfal",
        importOptions = ImportOption.DoNotIncludeTests.class
)
public class EndpointImplementationNamingTest {

    /**
     * Predicate that matches classes implementing at least one interface
     * whose simple name ends with {@code Endpoint}.
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
     * Ensures that all endpoint implementations are named consistently.
     * *
     * Classes that implement a {@code *Endpoint} interface and reside in
     * a boundary package must have a simple class name ending with
     * {@code Resource}.
     */
    @ArchTest
    static final ArchRule implementations_of_Endpoint_should_end_with_Resource =
            classes()
                    .that().areTopLevelClasses()
                    .and().resideInAnyPackage(
                            "de.remsfal.service.boundary..",
                            "de.remsfal.notification.boundary..",
                            "de.remsfal.ticketing.boundary.."
                    )
                    .and(IMPLEMENTS_ENDPOINT_INTERFACE)
                    .should().haveSimpleNameEndingWith("Resource");
}
