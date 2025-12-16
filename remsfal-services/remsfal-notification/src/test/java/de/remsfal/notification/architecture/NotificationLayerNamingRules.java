package de.remsfal.notification.architecture;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

/**
 * Defines layer-specific naming conventions for classes
 * within the notification module.
 * *
 * These rules enforce clear and consistent naming patterns
 * for boundary and control layer classes, making their
 * responsibilities immediately visible from the class name.
 */
public final class NotificationLayerNamingRules {

    private NotificationLayerNamingRules() {
    }

    /**
     * Boundary layer classes represent REST endpoints or messaging consumers.
     * *
     * All top-level classes in the notification boundary package must
     * end with {@code Resource} or {@code Consumer}.
     */
    @ArchTest
    static final ArchRule boundary_classes_should_be_resources_or_consumers =
            classes()
                    .that().resideInAnyPackage("de.remsfal.notification.boundary..")
                    .and().areTopLevelClasses()
                    .should().haveSimpleNameEndingWith("Resource")
                    .orShould().haveSimpleNameEndingWith("Consumer");

    /**
     * Control layer classes coordinate application logic and messaging.
     * *
     * All top-level classes in the notification control package must
     * end with {@code Controller}, {@code Producer}, or {@code Consumer}.
     */
    @ArchTest
    static final ArchRule control_classes_should_be_controllers_or_messaging_components =
            classes()
                    .that().resideInAnyPackage("de.remsfal.notification.control..")
                    .and().areTopLevelClasses()
                    .should().haveSimpleNameEndingWith("Controller")
                    .orShould().haveSimpleNameEndingWith("Producer")
                    .orShould().haveSimpleNameEndingWith("Consumer");
}
