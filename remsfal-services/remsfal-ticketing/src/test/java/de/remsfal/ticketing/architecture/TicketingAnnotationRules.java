package de.remsfal.ticketing.architecture;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.MappedSuperclass;
import jakarta.ws.rs.*;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

/**
 * Annotation usage rules for the Ticketing service.
 * These rules restrict the usage of framework-specific annotations
 * (JPA and JAX-RS) to well-defined architectural layers.
 * The goal is to:
 * - prevent framework leakage into unintended layers
 * - keep domain and control code free of infrastructure concerns
 * - ensure consistent structure across services
 */
public final class TicketingAnnotationRules {

    /**
     * Base package for all ticketing-related classes.
     */
    private static final String TICKETING_BASE = "de.remsfal.ticketing..";

    /**
     * Packages where JAX-RS annotations are allowed.
     * REST endpoints are implemented in the boundary layer,
     * while API contracts may be shared via the core API module.
     */
    private static final String[] ALLOWED_JAXRS_PACKAGES = {
            "de.remsfal.ticketing.boundary..",
            "de.remsfal.core.api.."
    };

    /**
     * Utility class – not meant to be instantiated.
     */
    private TicketingAnnotationRules() { }

    /**
     * Ensures that JPA annotations are only used in entity packages.
     * Classes annotated with {@link Entity}, {@link Embeddable},
     * or {@link MappedSuperclass} must reside in
     * {@code de.remsfal.ticketing.entity..}.
     */
    @ArchTest
    public static final ArchRule JPA_ANNOTATIONS_ONLY_IN_ENTITY_PACKAGES =
            classes()
                    .that().areAnnotatedWith(Entity.class)
                    .or().areAnnotatedWith(Embeddable.class)
                    .or().areAnnotatedWith(MappedSuperclass.class)
                    .should().resideInAnyPackage("de.remsfal.ticketing.entity..")
                    .allowEmptyShould(true);

    /**
     * Restricts the usage of {@link Path} annotations.
     * Classes annotated with {@code @Path} must only be located
     * in the boundary layer or in the shared core API module.
     */
    @ArchTest
    public static final ArchRule JAXRS_PATH_ONLY_IN_BOUNDARY_OR_CORE_API =
            classes()
                    .that().resideInAnyPackage(TICKETING_BASE)
                    .and().areAnnotatedWith(Path.class)
                    .should().resideInAnyPackage(ALLOWED_JAXRS_PACKAGES)
                    .allowEmptyShould(true);

    /**
     * Restricts HTTP method annotations to endpoint classes.
     * Methods annotated with {@link GET}, {@link POST}, {@link PUT},
     * {@link PATCH}, or {@link DELETE} must be declared in classes
     * located in the boundary layer or core API module.
     */
    @ArchTest
    public static final ArchRule JAXRS_HTTP_METHODS_ONLY_IN_BOUNDARY_OR_CORE_API =
            methods()
                    .that().areAnnotatedWith(GET.class)
                    .or().areAnnotatedWith(POST.class)
                    .or().areAnnotatedWith(PUT.class)
                    .or().areAnnotatedWith(PATCH.class)
                    .or().areAnnotatedWith(DELETE.class)
                    .and().areDeclaredInClassesThat().resideInAnyPackage(TICKETING_BASE)
                    .should().beDeclaredInClassesThat().resideInAnyPackage(ALLOWED_JAXRS_PACKAGES)
                    .allowEmptyShould(true);
}
