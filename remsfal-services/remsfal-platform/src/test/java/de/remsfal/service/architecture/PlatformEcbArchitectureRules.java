package de.remsfal.service.architecture;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * ECB (Entity–Control–Boundary) architecture rules for the Platform (service) module.
 * These rules enforce a strict separation of responsibilities between
 * boundary, control, and entity layers by defining explicit dependency
 * constraints.
 * The rules are whitelist-based: each layer may only access a clearly
 * defined set of allowed packages.
 */
public final class PlatformEcbArchitectureRules {

    /**
     * Base package for the platform/service module.
     */
    private static final String BASE = "de.remsfal.service..";

    /**
     * Packages that may be accessed by boundary layer classes.
     * Boundary components are allowed to:
     * - call control logic
     * - access DTOs, models and entities
     * - use shared core/common modules
     * - depend on framework and selected third-party libraries
     */
    private static final String[] ALLOWED_FOR_BOUNDARY = {
            "de.remsfal.service.boundary..",
            "de.remsfal.service.control..",
            "de.remsfal.service.dto..",
            "de.remsfal.service.model..",
            "de.remsfal.service.entity..",

            "de.remsfal.core..",
            "de.remsfal.common..",

            "java..",
            "jakarta..",
            "org.jboss.logging..",
            "io.quarkus..",
            "org.eclipse.microprofile..",
            "io.smallrye..",

            "com.google.api.client..",
            "com.nimbusds.."
    };

    /**
     * Packages that may be accessed by control layer classes.
     * Control components coordinate application logic and messaging.
     * They must not depend on boundary components and are restricted
     * to domain-related packages, shared modules and frameworks.
     */
    private static final String[] ALLOWED_FOR_CONTROL = {
            "de.remsfal.service.control..",
            "de.remsfal.service.control.event..",
            "de.remsfal.service.dto..",
            "de.remsfal.service.model..",
            "de.remsfal.service.entity..",

            "de.remsfal.core..",
            "de.remsfal.common..",

            "java..",
            "jakarta..",
            "org.jboss.logging..",
            "io.quarkus..",
            "org.eclipse.microprofile..",
            "io.smallrye..",

            "com.nimbusds.."
    };

    /**
     * Utility class – not meant to be instantiated.
     */
    private PlatformEcbArchitectureRules() { }

    /**
     * Ensures that boundary layer classes only access allowed packages.
     * This prevents boundary components from bypassing the control layer
     * or directly accessing forbidden infrastructure or implementation details.
     */
    @ArchTest
    public static final ArchRule boundary_should_only_access_allowed_packages =
            classes()
                    .that().resideInAnyPackage("de.remsfal.service.boundary..")
                    .and().resideInAnyPackage(BASE)
                    .should().onlyAccessClassesThat().resideInAnyPackage(ALLOWED_FOR_BOUNDARY)
                    .allowEmptyShould(true);

    /**
     * Ensures that control layer classes only access allowed packages.
     * In particular, control components must not depend on boundary classes.
     */
    @ArchTest
    public static final ArchRule control_should_only_access_allowed_packages =
            classes()
                    .that().resideInAnyPackage("de.remsfal.service.control..")
                    .and().resideInAnyPackage(BASE)
                    .should().onlyAccessClassesThat().resideInAnyPackage(ALLOWED_FOR_CONTROL)
                    .allowEmptyShould(true);

    /**
     * Explicitly forbids control layer access to boundary components.
     * This rule protects the direction of dependencies in the ECB architecture
     * and prevents cyclic dependencies between boundary and control layers.
     */
    @ArchTest
    public static final ArchRule control_should_not_access_boundary =
            noClasses()
                    .that().resideInAnyPackage("de.remsfal.service.control..")
                    .and().resideInAnyPackage(BASE)
                    .should().accessClassesThat().resideInAnyPackage("de.remsfal.service.boundary..")
                    .allowEmptyShould(true);

    /**
     * Ensures that entity classes are completely isolated from
     * boundary and control layers.
     * Entities represent pure domain state and must not depend
     * on application logic or API layers.
     */
    @ArchTest
    public static final ArchRule entity_should_not_access_boundary_or_control =
            noClasses()
                    .that().resideInAnyPackage("de.remsfal.service.entity..")
                    .and().resideInAnyPackage(BASE)
                    .should().accessClassesThat().resideInAnyPackage(
                            "de.remsfal.service.control..",
                            "de.remsfal.service.boundary.."
                    )
                    .allowEmptyShould(true);
}
