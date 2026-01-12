package de.remsfal.ticketing.architecture;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

public final class TicketingEcbArchitectureRules {

    private static final String BASE = "de.remsfal.ticketing..";

    private static final String[] ALLOWED_FOR_BOUNDARY = {
            "de.remsfal.ticketing.boundary..",
            "de.remsfal.ticketing.control..",
            "de.remsfal.ticketing.dto..",
            "de.remsfal.ticketing.model..",
            "de.remsfal.ticketing.entity..",
            "de.remsfal.ticketing.infrastructure..",

            "de.remsfal.core..",
            "de.remsfal.common..",

            "java..",
            "jakarta..",
            "org.jboss.logging..",
            "io.quarkus..",
            "org.eclipse.microprofile..",
            "io.smallrye..",
            "org.jboss.resteasy..",
            "com.fasterxml.jackson.."
    };

    private static final String[] ALLOWED_FOR_CONTROL = {
            "de.remsfal.ticketing.control..",
            "de.remsfal.ticketing.control.event..",
            "de.remsfal.ticketing.dto..",
            "de.remsfal.ticketing.model..",
            "de.remsfal.ticketing.entity..",
            "de.remsfal.ticketing.infrastructure..",

            "de.remsfal.core..",
            "de.remsfal.common..",

            "java..",
            "jakarta..",
            "org.jboss.logging..",
            "io.quarkus..",
            "org.eclipse.microprofile..",
            "io.smallrye.."
    };

    private TicketingEcbArchitectureRules() { }

    @ArchTest
    public static final ArchRule boundary_should_only_access_allowed_packages =
            classes()
                    .that().resideInAnyPackage("de.remsfal.ticketing.boundary..")
                    .and().resideInAnyPackage(BASE)
                    .should().onlyAccessClassesThat().resideInAnyPackage(ALLOWED_FOR_BOUNDARY)
                    .allowEmptyShould(true);

    @ArchTest
    public static final ArchRule control_should_only_access_allowed_packages =
            classes()
                    .that().resideInAnyPackage("de.remsfal.ticketing.control..")
                    .and().resideInAnyPackage(BASE)
                    .should().onlyAccessClassesThat().resideInAnyPackage(ALLOWED_FOR_CONTROL)
                    .allowEmptyShould(true);

    @ArchTest
    public static final ArchRule control_should_not_access_boundary =
            noClasses()
                    .that().resideInAnyPackage("de.remsfal.ticketing.control..")
                    .and().resideInAnyPackage(BASE)
                    .should().accessClassesThat().resideInAnyPackage("de.remsfal.ticketing.boundary..")
                    .allowEmptyShould(true);

    @ArchTest
    public static final ArchRule entity_should_not_access_boundary_or_control =
            noClasses()
                    .that().resideInAnyPackage("de.remsfal.ticketing.entity..")
                    .and().resideInAnyPackage(BASE)
                    .should().accessClassesThat().resideInAnyPackage(
                            "de.remsfal.ticketing.control..",
                            "de.remsfal.ticketing.boundary.."
                    )
                    .allowEmptyShould(true);
}
