package de.remsfal.ticketing.architecture;

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


public final class TicketingAnnotationRules {
    private TicketingAnnotationRules() {
    }

    public static final ArchRule JPA_ANNOTATIONS_ONLY_IN_ENTITY_PACKAGES =
            classes()
                    .that().areAnnotatedWith(Entity.class)
                    .or().areAnnotatedWith(Embeddable.class)
                    .or().areAnnotatedWith(MappedSuperclass.class)
                    .should().resideInAnyPackage(
                            "de.remsfal.ticketing.entity.."
                    );

    public static final ArchRule JAXRS_PATH_ONLY_IN_BOUNDARY =
            classes()
                    .that().areAnnotatedWith(Path.class)
                    .should().resideInAnyPackage(
                            "de.remsfal.ticketing.boundary.."
                    );

    public static final ArchRule JAXRS_HTTP_METHODS_ONLY_IN_BOUNDARY =
            methods()
                    .that().areAnnotatedWith(GET.class)
                    .or().areAnnotatedWith(POST.class)
                    .or().areAnnotatedWith(PUT.class)
                    .or().areAnnotatedWith(DELETE.class)
                    .should().beDeclaredInClassesThat().resideInAnyPackage(
                            "de.remsfal.ticketing.boundary.."
                    );
}
