package de.remsfal.test.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.junit.ArchTests;

/**
 * Aggregates ArchUnit rules that enforce layer-specific naming conventions
 * across different modules of the application.
 * *
 * This test class does not declare rules itself. Instead, it imports
 * rule sets from module-specific classes that define how classes in
 * certain layers (e.g. boundary, control, entity) have to be named.
 * *
 * Only production classes located under {@code de.remsfal} are analyzed.
 * Test classes are explicitly excluded from the analysis.
 */
@AnalyzeClasses(
        packages = "de.remsfal",
        importOptions = {
                ImportOption.DoNotIncludeTests.class
        }
)
class LayerNamingConventionsTests {

    /**
     * Executes layer naming convention rules defined for the platform/service module.
     */
    @ArchTest
    static final ArchTests SERVICE_NAMING_RULES =
            ArchTests.in(de.remsfal.test.plattform.architecture.PlattformLayerNamingRules.class);

    /**
     * Executes layer naming convention rules defined for the ticketing module.
     */
    @ArchTest
    static final ArchTests TICKETING_NAMING_RULES =
            ArchTests.in(de.remsfal.test.ticketing.architecture.TicketingLayerNamingRules.class);

    /**
     * Executes layer naming convention rules defined for the notification module.
     */
    @ArchTest
    static final ArchTests NOTIFICATION_NAMING_RULES =
            ArchTests.in(de.remsfal.test.notification.architecture.NotificationLayerNamingRules.class);
}
