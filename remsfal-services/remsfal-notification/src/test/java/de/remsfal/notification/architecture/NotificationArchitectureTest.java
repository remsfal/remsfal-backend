package de.remsfal.notification.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.junit.ArchTests;

/**
 * Aggregates ArchUnit rules that enforce architecture constraints
 * for the notification module.
 * *
 * This test class imports rule sets for annotation usage and
 * layer naming conventions specific to the notification service.
 * *
 * Only production classes located under {@code de.remsfal.notification} are analyzed.
 * Test classes are explicitly excluded from the analysis.
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
}
