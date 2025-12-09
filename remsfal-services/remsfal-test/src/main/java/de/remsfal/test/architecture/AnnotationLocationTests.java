package de.remsfal.test.architecture;


import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.junit.ArchTests;
import com.tngtech.archunit.core.importer.ImportOption;

/**
 * Aggregates all ArchUnit tests that verify the correct location and usage
 * of annotations across different modules of the application.
 * *
 * This test class does not define architecture rules itself.
 * Instead, it imports and executes annotation-related rule sets
 * from module-specific architecture test classes.
 * *
 * Only production classes located under {@code de.remsfal} are analyzed.
 * Test classes are explicitly excluded from the analysis.
 */
@AnalyzeClasses(
        packages = "de.remsfal",
        importOptions = { ImportOption.DoNotIncludeTests.class }
)
class AnnotationLocationTests {

    /**
     * Executes annotation architecture rules defined for the platform module.
     * *
     * These rules ensure that platform-specific annotations are used only
     * in their intended layers and packages.
     */
    @ArchTest
    static final ArchTests SERVICE_ANNOTATIONS =
            ArchTests.in(de.remsfal.test.plattform.architecture.PlattformAnnotationRules.class);

    /**
     * Executes annotation architecture rules defined for the notification module.
     * *
     * These rules validate the correct placement and usage of
     * notification-related annotations.
     */
    @ArchTest
    static final ArchTests NOTIFICATION_ANNOTATIONS =
            ArchTests.in(de.remsfal.test.notification.architecture.NotificationAnnotationRules.class);

    /**
     * Executes annotation architecture rules defined for the ticketing module.
     * *
     * These rules enforce architectural constraints regarding
     * ticketing-specific annotations and module boundaries.
     */
    @ArchTest
    static final ArchTests TICKETING_ANNOTATIONS =
            ArchTests.in(de.remsfal.test.ticketing.architecture.TicketingAnnotationRules.class);
}

