package de.remsfal.notification.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.junit.ArchTests;

/**
 * Central ArchUnit test suite for the Notification service.
 * This class aggregates all architecture rules that define and enforce
 * structural constraints for the notification module.
 * The following rule sets are executed:
 * - annotation usage rules
 * - layer and naming conventions
 * - endpoint implementation naming rules
 * - ECB (Entity–Control–Boundary) architecture rules
 * Only production classes located under {@code de.remsfal.notification}
 * are analyzed. Test classes are explicitly excluded.
 */
@AnalyzeClasses(
        packages = "de.remsfal.notification",
        importOptions = {
                ImportOption.DoNotIncludeTests.class
        }
)
class NotificationArchitectureTest {

    /**
     * Executes annotation architecture rules for the notification module.
     */
    @ArchTest
    static final ArchTests ANNOTATION_RULES =
            ArchTests.in(NotificationAnnotationRules.class);

    /**
     * Executes layer naming convention rules for the notification module.
     */
    @ArchTest
    static final ArchTests NAMING_RULES =
            ArchTests.in(NotificationLayerNamingRules.class);

    /**
     * Executes endpoint implementation naming rules.
     * Ensures that endpoint implementation classes follow
     * the defined naming conventions for the notification service.
     */
    @ArchTest
    static final ArchTests ENDPOINT_IMPL_RULES = ArchTests.in(NotificationEndpointImplementationNamingRules.class);

    /**
     * Executes ECB (Entity–Control–Boundary) architecture rules.
     * Enforces a strict separation of responsibilities between
     * entities, control logic, and boundary components.
     */
    @ArchTest
    static final ArchTests ECB_RULES = ArchTests.in(NotificationEcbArchitectureRules.class);

}
