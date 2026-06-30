package de.remsfal.notification.architecture;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import jakarta.ws.rs.Path;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

/**
 * Defines layer-specific naming conventions for the Notification service.
 * These rules enforce clear and consistent class naming based on
 * architectural responsibility (boundary, control, persistence).
 * Consistent naming helps to:
 * - make responsibilities visible at a glance
 * - prevent architectural drift
 * - improve readability and maintainability
 */
public final class NotificationLayerNamingRules {

    /**
     * Utility class – not meant to be instantiated.
     */
    private NotificationLayerNamingRules() {
    }

    /**
     * Boundary layer classes.
     * Boundary components represent REST endpoints ({@code *Resource}),
     * incoming Kafka message handlers ({@code *Consumer}),
     * or outgoing Kafka message wrappers ({@code *Producer}).
     * All three variants belong in the boundary layer of the ECB pattern:
     * REST resources reside in {@code boundary}, while Kafka consumers and producers
     * reside in the {@code boundary.eventing} sub-package.
     */
    @ArchTest
    static final ArchRule boundary_classes_should_follow_naming_convention =
            classes()
                    .that().resideInAnyPackage("de.remsfal.notification.boundary..")
                    .and().areTopLevelClasses()
                    .should().haveSimpleNameEndingWith("Resource")
                    .orShould().haveSimpleNameEndingWith("Consumer")
                    .orShould().haveSimpleNameEndingWith("Producer")
                    .allowEmptyShould(true);

    /**
     * REST endpoint naming rule.
     * Any boundary class annotated with {@link Path} represents
     * an HTTP endpoint and must therefore end with {@code Resource}.
     */
    @ArchTest
    static final ArchRule boundary_path_annotated_should_end_with_resource =
            classes()
                    .that().resideInAnyPackage("de.remsfal.notification.boundary..")
                    .and().areTopLevelClasses()
                    .and().areAnnotatedWith(Path.class)
                    .should().haveSimpleNameEndingWith("Resource")
                    .allowEmptyShould(true);

    /**
     * Control layer naming rules.
     * Control components encapsulate application logic or messaging behavior.
     * Their class names must clearly reflect their responsibility.
     */
    @ArchTest
    static final ArchRule control_classes_should_be_controllers_or_messaging_components =
            classes()
                    .that().resideInAnyPackage("de.remsfal.notification.control")
                    .and().areTopLevelClasses()
                    .should().haveSimpleNameEndingWith("Controller")
                    .orShould().haveSimpleNameEndingWith("Producer")
                    .orShould().haveSimpleNameEndingWith("Consumer")
                    .allowEmptyShould(true);
}
