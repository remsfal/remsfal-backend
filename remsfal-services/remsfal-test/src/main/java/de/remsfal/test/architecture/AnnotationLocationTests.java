package de.remsfal.test.architecture;


import com.tngtech.archunit.junit.AnalyzeClasses;
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

@AnalyzeClasses(
        packages = "de.remsfal",
        importOptions = {
                com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests.class
        }
)
class AnnotationLocationTests {

    /**
     * JPA-Annotations (@Entity, @Embeddable, @MappedSuperclass)
     * dürfen nur in Entity-Paketen verwendet werden.
     */
    @ArchTest
    static final ArchRule jpa_annotations_only_in_entity_packages =
            classes()
                    .that().areAnnotatedWith(Entity.class)
                    .or().areAnnotatedWith(Embeddable.class)
                    .or().areAnnotatedWith(MappedSuperclass.class)
                    .should().resideInAnyPackage(
                            "de.remsfal.service.entity..",
                            "de.remsfal.ticketing.entity.."
                    );

    /**
     * Klassen mit @Path dürfen nur in Boundary/API-Paketen liegen.
     * (inkl. de.remsfal.core.api.* wie in der Anmerkung gefordert)
     */
    @ArchTest
    static final ArchRule jaxrs_path_only_in_boundary_or_api =
            classes()
                    .that().areAnnotatedWith(Path.class)
                    .should().resideInAnyPackage(
                            "de.remsfal.service.boundary..",
                            "de.remsfal.notification.boundary..",
                            "de.remsfal.ticketing.boundary..",
                            "de.remsfal.core.api.."
                    );

    /**
     * HTTP-Methode-Annotations (@GET, @POST, …) dürfen nur
     * in Boundary/API-Paketen auf Methoden verwendet werden.
     */
    @ArchTest
    static final ArchRule jaxrs_http_methods_only_in_boundary_or_api =
            methods()
                    .that().areAnnotatedWith(GET.class)
                    .or().areAnnotatedWith(POST.class)
                    .or().areAnnotatedWith(PUT.class)
                    .or().areAnnotatedWith(DELETE.class)
                    .should().beDeclaredInClassesThat().resideInAnyPackage(
                            "de.remsfal.service.boundary..",
                            "de.remsfal.notification.boundary..",
                            "de.remsfal.ticketing.boundary..",
                            "de.remsfal.core.api.."
                    );
}

