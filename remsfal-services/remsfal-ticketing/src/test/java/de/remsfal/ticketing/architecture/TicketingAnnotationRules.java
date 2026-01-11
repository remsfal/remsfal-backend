package de.remsfal.ticketing.architecture;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.MappedSuperclass;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

/**
 * Defines annotation-related architecture rules for the ticketing module.
 * *
 * These rules restrict the usage of JPA and JAX-RS annotations to
 * dedicated layers in order to maintain a clear separation between
 * persistence, API and domain logic.
 */
public final class TicketingAnnotationRules {
    /**
     * Base package for the ticketing module.
     */
    private static final String TICKETING_BASE = "de.remsfal.ticketing..";

    private TicketingAnnotationRules() {
    }

    /**
     * Ensures that JPA-related annotations are only used in entity packages.
     * *
     * Classes within the ticketing module annotated with {@link Entity},
     * {@link Embeddable} or {@link MappedSuperclass} must reside in
     * {@code de.remsfal.ticketing.entity..}.
     */
    @ArchTest
    public static final ArchRule JPA_ANNOTATIONS_ONLY_IN_ENTITY_PACKAGES =
            classes()
                    .that().resideInAnyPackage(TICKETING_BASE)
                    .and().areAnnotatedWith(Entity.class)
                    .or().resideInAnyPackage(TICKETING_BASE)
                    .and().areAnnotatedWith(Embeddable.class)
                    .or().resideInAnyPackage(TICKETING_BASE)
                    .and().areAnnotatedWith(MappedSuperclass.class)
                    .should().resideInAnyPackage(
                            "de.remsfal.ticketing.entity.."
                    );

    /**
     * Ensures that {@link Path} annotations are used only in the
     * ticketing boundary layer.
     * *
     * {@code allowEmptyShould(true)} is used to avoid failures when
     * the module does not yet contain any REST endpoints.
     */
    @ArchTest
    public static final ArchRule JAXRS_PATH_ONLY_IN_BOUNDARY =
            classes()
                    .that().resideInAnyPackage(TICKETING_BASE)
                    .and().areAnnotatedWith(Path.class)
                    .should().resideInAnyPackage(
                            "de.remsfal.ticketing.boundary.."
                    )
                    .allowEmptyShould(true);

    /**
     * Ensures that JAX-RS HTTP method annotations are only declared
     * in boundary layer classes within the ticketing module.
     * *
     * Methods annotated with {@link GET}, {@link POST}, {@link PUT} or
     * {@link DELETE} must belong to classes in
     * {@code de.remsfal.ticketing.boundary..}.
     */
    @ArchTest
    public static final ArchRule JAXRS_HTTP_METHODS_ONLY_IN_BOUNDARY =
            methods()
                    .that().areDeclaredInClassesThat().resideInAnyPackage(TICKETING_BASE)
                    .and().areAnnotatedWith(GET.class)
                    .or().areDeclaredInClassesThat().resideInAnyPackage(TICKETING_BASE)
                    .and().areAnnotatedWith(POST.class)
                    .or().areDeclaredInClassesThat().resideInAnyPackage(TICKETING_BASE)
                    .and().areAnnotatedWith(PUT.class)
                    .or().areDeclaredInClassesThat().resideInAnyPackage(TICKETING_BASE)
                    .and().areAnnotatedWith(DELETE.class)
                    .should().beDeclaredInClassesThat().resideInAnyPackage(
                            "de.remsfal.ticketing.boundary.."
                    )
                    .allowEmptyShould(true);
}
