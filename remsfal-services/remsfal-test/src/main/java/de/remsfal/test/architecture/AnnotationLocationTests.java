package de.remsfal.test.architecture;


import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.junit.ArchTests;
import com.tngtech.archunit.core.importer.ImportOption;

@AnalyzeClasses(
        packages = "de.remsfal",
        importOptions = { ImportOption.DoNotIncludeTests.class }
)
class AnnotationLocationTests {

    // Regeln aus dem service/platform-Modul
    @ArchTest
    static final ArchTests SERVICE_ANNOTATIONS =
            ArchTests.in(de.remsfal.service.architecture.ServiceAnnotationRules.class);

    // Regeln aus dem notification-Modul
    @ArchTest
    static final ArchTests NOTIFICATION_ANNOTATIONS =
            ArchTests.in(de.remsfal.notification.architecture.NotificationAnnotationRules.class);

    // Regeln aus dem ticketing-Modul
    @ArchTest
    static final ArchTests TICKETING_ANNOTATIONS =
            ArchTests.in(de.remsfal.ticketing.architecture.TicketingAnnotationRules.class);
}

