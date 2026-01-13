package de.remsfal.service.architecture;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.MappedSuperclass;
import jakarta.ws.rs.*;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

/**
 * Defines annotation-related architecture rules for the Platform (service) module.
 * These rules restrict the usage of framework-specific annotations
 * (e.g. JPA and JAX-RS) to their designated architectural layers.
 * The goal is to enforce a clean separation between:
 * - persistence concerns
 * - API/boundary concerns
 * - internal application logic
 */
public final class PlatformAnnotationRules {

    /**
     * Base package for the platform/service module.
     */
    private static final String SERVICE_BASE = "de.remsfal.service..";

    /**
     * Packages in which JAX-RS annotations are allowed.
     * - boundary: concrete REST endpoint implementations
     * - core API: shared endpoint contracts
     */
    private static final String[] ALLOWED_JAXRS_PACKAGES = {
            "de.remsfal.service.boundary..",
            "de.remsfal.core.api.."
    };

    /**
     * Utility class – not meant to be instantiated.
     */
    private PlatformAnnotationRules() { }

    /**
     * Ensures that JPA-related annotations are only used in entity packages.
     * Classes annotated with {@link Entity}, {@link Embeddable} or
     * {@link MappedSuperclass} must reside in
     * {@code de.remsfal.service.entity..}.
     * This prevents persistence annotations from leaking into
     * boundary or control layers.
     */
    @ArchTest
    public static final ArchRule JPA_ANNOTATIONS_ONLY_IN_ENTITY_PACKAGES =
            classes()
                    .that().areAnnotatedWith(Entity.class)
                    .or().areAnnotatedWith(Embeddable.class)
                    .or().areAnnotatedWith(MappedSuperclass.class)
                    .should().resideInAnyPackage("de.remsfal.service.entity..")
                    .allowEmptyShould(true);

    /**
     * Ensures that {@link Path} annotations are only used in allowed packages.
     * REST endpoint definitions must either:
     * - reside in the platform boundary layer, or
     * - be declared as shared API contracts in the core module.
     */
    @ArchTest
    public static final ArchRule JAXRS_PATH_ONLY_IN_BOUNDARY_OR_CORE_API =
            classes()
                    .that().resideInAnyPackage(SERVICE_BASE)
                    .and().areAnnotatedWith(Path.class)
                    .should().resideInAnyPackage(ALLOWED_JAXRS_PACKAGES)
                    .allowEmptyShould(true);

    /**
     * Ensures that JAX-RS HTTP method annotations are only declared
     * in boundary or core API classes.
     * This rule applies to the following annotations:
     * {@link GET}, {@link POST}, {@link PUT}, {@link PATCH}, {@link DELETE}.
     * It prevents HTTP endpoint logic from being implemented
     * in control, entity or infrastructure layers.
     */
    @ArchTest
    public static final ArchRule JAXRS_HTTP_METHODS_ONLY_IN_BOUNDARY_OR_CORE_API =
            methods()
                    .that().areAnnotatedWith(GET.class)
                    .or().areAnnotatedWith(POST.class)
                    .or().areAnnotatedWith(PUT.class)
                    .or().areAnnotatedWith(PATCH.class)
                    .or().areAnnotatedWith(DELETE.class)
                    .and().areDeclaredInClassesThat().resideInAnyPackage(SERVICE_BASE)
                    .should().beDeclaredInClassesThat().resideInAnyPackage(ALLOWED_JAXRS_PACKAGES)
                    .allowEmptyShould(true);
}
