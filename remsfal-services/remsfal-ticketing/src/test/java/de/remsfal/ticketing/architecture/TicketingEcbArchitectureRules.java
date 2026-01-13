package de.remsfal.ticketing.architecture;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * ECB (Entity–Control–Boundary) architecture rules for the Ticketing service.
 * These rules enforce a strict layering model by explicitly defining
 * which packages may be accessed by each architectural layer.
 * The architecture is enforced using a whitelist approach:
 * each layer may only access a predefined set of packages.
 */
public final class TicketingEcbArchitectureRules {

    /**
     * Base package for all ticketing-related classes.
     */
    private static final String BASE = "de.remsfal.ticketing..";

    /**
     * Packages that may be accessed by boundary layer classes.
     * Boundary components act as entry points (REST endpoints, adapters)
     * and are therefore allowed to depend on control logic, domain models,
     * infrastructure components, shared modules and frameworks.
     */
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

    /**
     * Packages that may be accessed by control layer classes.
     * Control components implement application logic and orchestration.
     * They must not depend on boundary components but may use
     * infrastructure, domain and shared modules.
     */
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
            "org.jboss.resteasy..",
            "io.quarkus..",
            "org.eclipse.microprofile..",
            "io.smallrye.."
    };

    /**
     * Utility class – not meant to be instantiated.
     */
    private TicketingEcbArchitectureRules() { }

    /**
     * Ensures that boundary layer classes only access allowed packages.
     * This rule prevents boundary components from bypassing the control
     * layer or depending on unintended implementation details.
     */
    @ArchTest
    public static final ArchRule boundary_should_only_access_allowed_packages =
            classes()
                    .that().resideInAnyPackage("de.remsfal.ticketing.boundary..")
                    .and().resideInAnyPackage(BASE)
                    .should().onlyAccessClassesThat().resideInAnyPackage(ALLOWED_FOR_BOUNDARY)
                    .allowEmptyShould(true);

    /**
     * Ensures that control layer classes only access allowed packages.
     * This keeps application logic independent of the API/boundary layer.
     */
    @ArchTest
    public static final ArchRule control_should_only_access_allowed_packages =
            classes()
                    .that().resideInAnyPackage("de.remsfal.ticketing.control..")
                    .and().resideInAnyPackage(BASE)
                    .should().onlyAccessClassesThat().resideInAnyPackage(ALLOWED_FOR_CONTROL)
                    .allowEmptyShould(true);

    /**
     * Explicitly forbids control layer access to boundary components.
     * This rule enforces the direction of dependencies and prevents
     * cyclic dependencies between boundary and control layers.
     */
    @ArchTest
    public static final ArchRule control_should_not_access_boundary =
            noClasses()
                    .that().resideInAnyPackage("de.remsfal.ticketing.control..")
                    .and().resideInAnyPackage(BASE)
                    .should().accessClassesThat().resideInAnyPackage("de.remsfal.ticketing.boundary..")
                    .allowEmptyShould(true);

    /**
     * Ensures that entity classes are fully isolated from
     * boundary and control layers.
     * Entities represent pure domain state and must not depend
     * on application logic or API layers.
     */
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
