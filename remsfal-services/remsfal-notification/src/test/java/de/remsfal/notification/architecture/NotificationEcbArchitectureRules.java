package de.remsfal.notification.architecture;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * ArchUnit rules enforcing the ECB (Entity–Control–Boundary) architecture
 * for the Notification service.
 * These rules define allowed dependencies between layers and ensure
 * a strict separation of responsibilities:
 * - Boundary: transport and API layer
 * - Control: application logic
 * - Entity: domain and persistence model
 */
public final class NotificationEcbArchitectureRules {

    /**
     * Base package of the Notification service.
     */
    private static final String BASE = "de.remsfal.notification..";

    /**
     * Whitelist of packages that may be accessed by boundary classes.
     * Boundary classes are allowed to depend on control, DTOs,
     * domain models, entities, and shared infrastructure libraries,
     * but must not access implementation details outside these packages.
     */
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

    /**
     * Whitelist of packages that may be accessed by control classes.
     * Control classes may depend on DTOs, domain models, entities,
     * and shared core/common libraries, but must remain independent
     * of boundary (transport-layer) implementations.
     */
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

    /**
     * Utility class – not meant to be instantiated.
     */
    private NotificationEcbArchitectureRules() { }

    /**
     * Ensures that boundary classes only access explicitly allowed packages.
     * This rule prevents the boundary layer from introducing
     * unintended dependencies on internal or unrelated packages.
     */
    @ArchTest
    public static final ArchRule boundary_should_only_access_allowed_packages =
            classes()
                    .that().resideInAnyPackage("de.remsfal.notification.boundary..")
                    .and().resideInAnyPackage(BASE)
                    .should().onlyAccessClassesThat().resideInAnyPackage(ALLOWED_FOR_BOUNDARY)
                    .allowEmptyShould(true);

    /**
     * Ensures that control classes only access explicitly allowed packages.
     * This rule enforces a clean application-layer boundary by
     * preventing control logic from depending on disallowed packages.
     */
    @ArchTest
    public static final ArchRule control_should_only_access_allowed_packages =
            classes()
                    .that().resideInAnyPackage("de.remsfal.notification.control..")
                    .and().resideInAnyPackage(BASE)
                    .should().onlyAccessClassesThat().resideInAnyPackage(ALLOWED_FOR_CONTROL)
                    .allowEmptyShould(true);

    /**
     * Ensures that control classes do not access boundary classes.
     * This enforces the architectural rule that application logic
     * must remain independent of transport-layer concerns.
     */
    @ArchTest
    public static final ArchRule control_should_not_access_boundary =
            noClasses()
                    .that().resideInAnyPackage("de.remsfal.notification.control..")
                    .and().resideInAnyPackage(BASE)
                    .should().accessClassesThat().resideInAnyPackage("de.remsfal.notification.boundary..")
                    .allowEmptyShould(true);

    /**
     * Ensures that entity classes do not depend on boundary or control layers.
     * Entities must remain free of application and transport-layer
     * dependencies to preserve domain purity and reusability.
     */
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
