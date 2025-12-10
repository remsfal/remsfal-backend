package de.remsfal.test.plattform.architecture;

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
 * Defines annotation-related architecture rules for the platform/service module.
 * *
 * These rules restrict the usage of JPA and JAX-RS annotations to dedicated
 * layers and packages in order to keep persistence, domain and API concerns
 * clearly separated.
 */
public final class PlattformAnnotationRules {

    /**
     * Base package for the service module.
     */
    private static final String SERVICE_BASE = "de.remsfal.service..";

    private PlattformAnnotationRules() { }

    /**
     * Ensures that JPA-related annotations are only used in entity packages.
     * *
     * Classes annotated with {@link Entity}, {@link Embeddable} or
     * {@link MappedSuperclass} must reside in the
     * {@code de.remsfal.service.entity..} package hierarchy.
     */
    @ArchTest
    public static final ArchRule JPA_ANNOTATIONS_ONLY_IN_ENTITY_PACKAGES =
            classes()
                    .that().areAnnotatedWith(Entity.class)
                    .or().areAnnotatedWith(Embeddable.class)
                    .or().areAnnotatedWith(MappedSuperclass.class)
                    .should().resideInAnyPackage("de.remsfal.service.entity..");

    /**
     * Ensures that {@link Path} annotations within the service module
     * are only used in boundary or core API packages.
     * *
     * This prevents REST endpoints from being declared in control,
     * entity or other internal layers.
     * *
     * {@code allowEmptyShould(true)} is used to avoid failures when
     * there are (yet) no {@code @Path}-annotated classes in the module.
     */
    @ArchTest
    public static final ArchRule JAXRS_PATH_ONLY_IN_BOUNDARY_OR_API =
            classes()
                    .that().resideInAnyPackage(SERVICE_BASE)
                    .and().areAnnotatedWith(Path.class)
                    .should().resideInAnyPackage(
                            "de.remsfal.service.boundary..",
                            "de.remsfal.core.api.."
                    )
                    // <-- wichtig: sonst Fehler, wenn es (noch) keine @Path-Klassen im service-Modul gibt
                    .allowEmptyShould(true);

    /**
     * Ensures that JAX-RS HTTP method annotations within the service module
     * are only used in boundary or core API classes.
     * *
     * Only methods declared in {@code de.remsfal.service..} are considered.
     * Any such method annotated with {@link GET}, {@link POST},
     * {@link PUT} or {@link DELETE} must be declared in a boundary or
     * core API class.
     * *
     * {@code allowEmptyShould(true)} allows the rule to pass even if
     * there are no HTTP-method-annotated endpoints yet.
     */
    @ArchTest
    public static final ArchRule JAXRS_HTTP_METHODS_ONLY_IN_BOUNDARY_OR_API =
            methods()
                    // immer nur Methoden aus dem service-Modul betrachten:
                    .that().areDeclaredInClassesThat().resideInAnyPackage(SERVICE_BASE)
                    .and().areAnnotatedWith(GET.class)
                    .or().areDeclaredInClassesThat().resideInAnyPackage(SERVICE_BASE)
                    .and().areAnnotatedWith(POST.class)
                    .or().areDeclaredInClassesThat().resideInAnyPackage(SERVICE_BASE)
                    .and().areAnnotatedWith(PUT.class)
                    .or().areDeclaredInClassesThat().resideInAnyPackage(SERVICE_BASE)
                    .and().areAnnotatedWith(DELETE.class)
                    .should().beDeclaredInClassesThat().resideInAnyPackage(
                            "de.remsfal.service.boundary..",
                            "de.remsfal.core.api.."
                    )
                    .allowEmptyShould(true);
}
