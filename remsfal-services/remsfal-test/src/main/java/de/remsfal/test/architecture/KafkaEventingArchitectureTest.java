package de.remsfal.test.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Verifies that Kafka Consumer and Producer classes are consistently placed
 * in the {@code boundary.eventing} sub-package of their respective service,
 * in accordance with the ECB (Entity–Control–Boundary) architecture pattern.
 * <p>
 * Consumers (incoming Kafka messages) and Producers (outgoing Kafka messages)
 * represent external system interactions and therefore belong in the Boundary layer.
 * The {@code boundary.eventing} sub-package separates them from REST Resources
 * in the {@code boundary} package, mirroring {@code de.remsfal.core.json.eventing}.
 * <p>
 * Classes with business logic that happen to trigger Kafka events (e.g. Controllers
 * or Enrichers) are explicitly excluded from these rules via name-based filtering.
 */
@AnalyzeClasses(
        packages = "de.remsfal",
        importOptions = ImportOption.DoNotIncludeTests.class
)
public final class KafkaEventingArchitectureTest {

    private static final String[] SERVICE_PACKAGES = {
        "de.remsfal.service..",
        "de.remsfal.notification..",
        "de.remsfal.ticketing.."
    };

    private KafkaEventingArchitectureTest() {
        // utility class
    }

    /**
     * Ensures that all Kafka consumer classes reside in the {@code boundary.eventing} package.
     * Consumer classes are identified by their {@code *Consumer} name suffix.
     * Placing consumers in {@code boundary.eventing} makes the incoming event boundary explicit
     * and separates them from REST resources in the {@code boundary} package.
     */
    @ArchTest
    static final ArchRule consumer_classes_should_reside_in_boundary_eventing =
            classes()
                    .that().haveSimpleNameEndingWith("Consumer")
                    .and().areTopLevelClasses()
                    .and().resideInAnyPackage(SERVICE_PACKAGES)
                    .should().resideInAPackage("..boundary.eventing..")
                    .allowEmptyShould(true);

    /**
     * Ensures that all Kafka producer classes reside in the {@code boundary.eventing} package.
     * Producer classes are identified by their {@code *Producer} name suffix.
     * Placing producers in {@code boundary.eventing} makes the outgoing event boundary explicit
     * and separates them from REST resources in the {@code boundary} package.
     */
    @ArchTest
    static final ArchRule producer_classes_should_reside_in_boundary_eventing =
            classes()
                    .that().haveSimpleNameEndingWith("Producer")
                    .and().areTopLevelClasses()
                    .and().resideInAnyPackage(SERVICE_PACKAGES)
                    .should().resideInAPackage("..boundary.eventing..")
                    .allowEmptyShould(true);

    /**
     * Explicitly prohibits placing Kafka consumer classes in the control layer.
     * The control layer must contain only business logic and must not act as
     * a Kafka entry point (incoming message boundary).
     */
    @ArchTest
    static final ArchRule no_consumer_classes_in_control =
            noClasses()
                    .that().haveSimpleNameEndingWith("Consumer")
                    .and().areTopLevelClasses()
                    .and().resideInAnyPackage(SERVICE_PACKAGES)
                    .should().resideInAPackage("..control..")
                    .allowEmptyShould(true);

    /**
     * Explicitly prohibits placing Kafka producer classes in the control layer.
     * The control layer must contain only business logic and must not act as
     * a thin Kafka output wrapper (outgoing message boundary).
     */
    @ArchTest
    static final ArchRule no_producer_classes_in_control =
            noClasses()
                    .that().haveSimpleNameEndingWith("Producer")
                    .and().areTopLevelClasses()
                    .and().resideInAnyPackage(SERVICE_PACKAGES)
                    .should().resideInAPackage("..control..")
                    .allowEmptyShould(true);

    /**
     * Ensures that every top-level class in {@code boundary.eventing} follows the
     * {@code *Consumer} or {@code *Producer} naming convention.
     * This keeps the eventing sub-package focused and prevents unrelated classes
     * (e.g. helpers, configs) from accumulating there without a proper layer assignment.
     */
    @ArchTest
    static final ArchRule boundary_eventing_classes_should_be_consumer_or_producer =
            classes()
                    .that().resideInAPackage("..boundary.eventing..")
                    .and().areTopLevelClasses()
                    .and().resideInAnyPackage(SERVICE_PACKAGES)
                    .should().haveSimpleNameEndingWith("Consumer")
                    .orShould().haveSimpleNameEndingWith("Producer")
                    .allowEmptyShould(true);

}
