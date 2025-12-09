package de.remsfal.service.architecture;

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

public final class ServiceAnnotationRules {
    private ServiceAnnotationRules() {
    }

    /**
     * JPA-Annotations (@Entity, @Embeddable, @MappedSuperclass)
     * dürfen nur unter de.remsfal.service.entity.. verwendet werden.
     */
    public static final ArchRule JPA_ANNOTATIONS_ONLY_IN_ENTITY_PACKAGES =
            classes()
                    .that().areAnnotatedWith(Entity.class)
                    .or().areAnnotatedWith(Embeddable.class)
                    .or().areAnnotatedWith(MappedSuperclass.class)
                    .should().resideInAnyPackage(
                            "de.remsfal.service.entity.."
                    );

    /**
     * @Path nur in Boundary/API (inkl. core.api).
     */
    public static final ArchRule JAXRS_PATH_ONLY_IN_BOUNDARY_OR_API =
            classes()
                    .that().areAnnotatedWith(Path.class)
                    .should().resideInAnyPackage(
                            "de.remsfal.service.boundary..",
                            "de.remsfal.core.api.."
                    );

    /**
     * HTTP-Methode-Annotations (@GET, @POST, …) nur in Boundary/API.
     */
    public static final ArchRule JAXRS_HTTP_METHODS_ONLY_IN_BOUNDARY_OR_API =
            methods()
                    .that().areAnnotatedWith(GET.class)
                    .or().areAnnotatedWith(POST.class)
                    .or().areAnnotatedWith(PUT.class)
                    .or().areAnnotatedWith(DELETE.class)
                    .should().beDeclaredInClassesThat().resideInAnyPackage(
                            "de.remsfal.service.boundary..",
                            "de.remsfal.core.api.."
                    );
}
