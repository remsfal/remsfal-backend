package de.remsfal.notification.architecture;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

public final class NotificationEcbArchitectureRules {

    private static final String BASE = "de.remsfal.notification..";

    private static final String[] ALLOWED_FOR_BOUNDARY = {
            "de.remsfal.notification.boundary..",
            "de.remsfal.notification.control..",
            "de.remsfal.notification.dto..",
            "de.remsfal.notification.model..",
            "de.remsfal.notification.entity..",

            "de.remsfal.core..",
            "de.remsfal.common..",

            "java..",
            "jakarta..",
            "org.jboss.logging..",
            "io.quarkus..",
            "org.eclipse.microprofile..",
            "io.smallrye.."
    };

    private static final String[] ALLOWED_FOR_CONTROL = {
            "de.remsfal.notification.control..",
            "de.remsfal.notification.control.event..",
            "de.remsfal.notification.dto..",
            "de.remsfal.notification.model..",
            "de.remsfal.notification.entity..",

            "de.remsfal.core..",
            "de.remsfal.common..",

            "java..",
            "jakarta..",
            "org.jboss.logging..",
            "io.quarkus..",
            "org.eclipse.microprofile..",
            "io.smallrye.."
    };

    private NotificationEcbArchitectureRules() { }

    @ArchTest
    public static final ArchRule boundary_should_only_access_allowed_packages =
            classes()
                    .that().resideInAnyPackage("de.remsfal.notification.boundary..")
                    .and().resideInAnyPackage(BASE)
                    .should().onlyAccessClassesThat().resideInAnyPackage(ALLOWED_FOR_BOUNDARY)
                    .allowEmptyShould(true);

    @ArchTest
    public static final ArchRule control_should_only_access_allowed_packages =
            classes()
                    .that().resideInAnyPackage("de.remsfal.notification.control..")
                    .and().resideInAnyPackage(BASE)
                    .should().onlyAccessClassesThat().resideInAnyPackage(ALLOWED_FOR_CONTROL)
                    .allowEmptyShould(true);

    @ArchTest
    public static final ArchRule control_should_not_access_boundary =
            noClasses()
                    .that().resideInAnyPackage("de.remsfal.notification.control..")
                    .and().resideInAnyPackage(BASE)
                    .should().accessClassesThat().resideInAnyPackage("de.remsfal.notification.boundary..")
                    .allowEmptyShould(true);

    @ArchTest
    public static final ArchRule entity_should_not_access_boundary_or_control =
            noClasses()
                    .that().resideInAnyPackage("de.remsfal.notification.entity..")
                    .and().resideInAnyPackage(BASE)
                    .should().accessClassesThat().resideInAnyPackage(
                            "de.remsfal.notification.control..",
                            "de.remsfal.notification.boundary.."
                    )
                    .allowEmptyShould(true);
}
